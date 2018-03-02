#!/bin/bash -l

#SBATCH --partition=discovery_q
#SBATCH --nodes=1
#SBATCH --job-name=s2mapping
#SBATCH --array=0-40
#SBATCH --exclusive
#SBATCH -t 24:00:00
#SBATCH --mem 32000

####SLURM_ARRAY_TASK_ID=1


module load Python/2.7.13-foss-2017a
#module load parallel/20160622-foss-2017a

ulimit -f unlimited
ulimit -t unlimited
ulimit -v unlimited
ulimit -u 32000

JOBROOT=/home/zreyn/s2mapping
ZIPS_PER_NODE=16
PROCESSES_PER_NODE=16

CURRENT=0
CHUNK_SIZE=16
ZIP_FILE=/scratch/s2/problem-zips.txt

cd $JOBROOT
COMMAND_FILE=$JOBROOT/commands_$SLURM_ARRAY_TASK_ID.commands
rm $COMMAND_FILE

CMD="python rerun-problem-zips.py"

MAIN_START=$((SLURM_ARRAY_TASK_ID*ZIPS_PER_NODE))

while [ $CURRENT -lt $PROCESSES_PER_NODE ]; do
        START=$((CURRENT*CHUNK_SIZE+MAIN_START))
        STOP=$((START+CHUNK_SIZE))
        OCMD="$CMD $ZIP_FILE out_data_rerun $START $((START+ZIPS_PER_NODE)) &"
        echo $OCMD >> $COMMAND_FILE
        eval $OCMD
        ((CURRENT++))
done;

#parallel --linebuffer --progress --jobs $PROCESSES_PER_NODE < $COMMAND_FILE
wait
