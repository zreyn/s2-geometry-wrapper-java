import argparse
import subprocess as process
import csv
import itertools

def main(start, stop, outfile):

    with open('data/zip-mapping.csv', 'rb') as csvfile:
        zctareader = csv.reader(csvfile, delimiter=',')
        for row in zctareader:
            # country,state_fips,county_fips,zcta
            zcta = row[3]
            if int(zcta) >= start and int(zcta) < stop:
                # do the covering
                command = 'java -jar coveringutil.jar getCovering data/tl_2015_us_zcta510.shp "ZCTA5CE10 = '+zcta+'" 15'
                p = process.Popen(command, shell=True, stdin=process.PIPE, stdout=process.PIPE, stderr=process.STDOUT, close_fds=True)
                output = p.stdout.read()
                cells = output.split()

                # write the cells + the row to file
                with open(outfile, 'a') as o:
                    writer = csv.writer(o, delimiter=',', lineterminator='\n')
                    for cell in cells:
                        writer.writerow([cell] + row)

if __name__ == '__main__':

        parser = argparse.ArgumentParser(
                    usage='usage: ./s2cellmapping.py <start> <stop> <outputfile>'
                    )

        parser.add_argument('start', type=int)
        parser.add_argument('stop', type=int)
        parser.add_argument('outfile', type=str)

        # parse the command line args
        args = parser.parse_args()

        main(args.start, args.stop, args.outfile)
