#!/bin/bash -l

#SBATCH --partition=discovery_q
#SBATCH --nodes=1
#SBATCH --job-name=s2mapping
#SBATCH --array=0-80
#SBATCH --exclusive
#SBATCH -t 24:00:00

####SLURM_ARRAY_TASK_ID=1


module load Python/2.7.13-foss-2017a
#module load parallel/20160622-foss-2017a

ulimit -f unlimited
ulimit -t unlimited
ulimit -v unlimited
ulimit -u 32000

JOBROOT=/home/zreyn/s2mapping
ZIPS_PER_NODE=1250
PROCESSES_PER_NODE=10

CURRENT=0
CHUNK_SIZE=250

cd $JOBROOT
COMMAND_FILE=$JOBROOT/commands_$SLURM_ARRAY_TASK_ID.commands
rm $COMMAND_FILE

#CMD="python s2cellmapping.py 95453 100000 out_data/99999.csv"
CMD="python s2cellmapping.py"

MAIN_START=$((SLURM_ARRAY_TASK_ID*ZIPS_PER_NODE))

while [ $CURRENT -lt $PROCESSES_PER_NODE ]; do
        START=$((CURRENT*CHUNK_SIZE+MAIN_START))
        STOP=$((START+CHUNK_SIZE))
        OCMD="$CMD $START $STOP out_data/$STOP.csv &"
        echo $OCMD >> $COMMAND_FILE
        eval $OCMD
        ((CURRENT++))
done;

#parallel --linebuffer --progress --jobs $PROCESSES_PER_NODE < $COMMAND_FILE
wait
