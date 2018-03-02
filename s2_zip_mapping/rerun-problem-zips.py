import argparse
import subprocess as process
import csv
import itertools
import os

def main(zipfile, outdir, start, stop):

    zips = []
    with open(zipfile) as f:
        zips = f.readlines()
    zips = [x.strip() for x in zips]

    with open('data/zip-mapping.csv', 'rb') as csvfile:
        zctareader = csv.reader(csvfile, delimiter=',')
        for row in zctareader:
            # country,state_fips,county_fips,zcta
            zcta = row[3]
            if zcta in zips[start:stop]:
                print zcta

                # do the covering
                command = 'java -jar coveringutil.jar getCovering data/tl_2015_us_zcta510.shp "ZCTA5CE10 = '+zcta+'" 15'
                p = process.Popen(command, shell=True, stdin=process.PIPE, stdout=process.PIPE, stderr=process.STDOUT, close_fds=True)
                output = p.stdout.read()
                cells = output.split()

                # write the cells + the row to file
                with open(os.path.join(outdir, '_'+zcta+'.csv'), 'w') as o:
                    writer = csv.writer(o, delimiter=',', lineterminator='\n')
                    for cell in cells:
                        if len(cell) == 9: # l15 cells are 9 chars long
                            writer.writerow([cell] + row)
                        else:
                            writer.write(output)
                            break

if __name__ == '__main__':

        parser = argparse.ArgumentParser(
                    usage='usage: ./rerun-problem-zips.py <zipfile> <outdir>'
                    )

        parser.add_argument('zipfile', type=str)
        parser.add_argument('outdir', type=str)
        parser.add_argument('start', type=int)
        parser.add_argument('stop', type=int)

        # parse the command line args
        args = parser.parse_args()

        main(args.zipfile, args.outdir, args.start, args.stop)
