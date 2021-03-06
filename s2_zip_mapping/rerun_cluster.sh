#!/bin/bash -l

#SBATCH --partition=discovery_q
#SBATCH --nodes=1
#SBATCH --job-name=s2mapping
#SBATCH --array=0-31
#SBATCH --exclusive
#SBATCH -t 24:00:00

####SLURM_ARRAY_TASK_ID=1


module load Python/2.7.13-foss-2017a
#module load parallel/20160622-foss-2017a

ulimit -f unlimited
ulimit -t unlimited
ulimit -v unlimited
ulimit -u 64000

JOBROOT=/home/zreyn/s2mapping
ZIPS_PER_NODE=16
PROCESSES_PER_NODE=16

CURRENT=0
CHUNK_SIZE=1
SCRATCH_FILE=/scratch/s2/s2mapping.tar.gz
LOCAL_SCRATCH=/localscratch/s2
FILE=/localscratch/s2/s2mapping.tar.gz
ZIP_FILE=/scratch/s2/problem-zips.txt
LOCAL_ZIP_FILE=/localscratch/s2/problem-zips.txt

cd $JOBROOT
COMMAND_FILE=$JOBROOT/commands_$SLURM_ARRAY_TASK_ID.commands
rm $COMMAND_FILE

mkdir $LOCAL_SCRATCH;
cd $LOCAL_SCRATCH
#if [ $(stat -c %s $SCRATCH_FILE) -ne $(stat -c %s $FILE) ]; then
        echo "Copying File $SCRATCH_FILE to $FILE";
        mkdir $LOCAL_SCRATCH;
        cp $SCRATCH_FILE $FILE;
        tar xvzf $FILE -C $LOCAL_SCRATCH;
        cp $ZIP_FILE $LOCAL_ZIP_FILE;
#fi

#CMD="python s2cellmapping.py 95453 100000 out_data/99999.csv"
CMD="python rerun-problem-zips.py"

MAIN_START=$((SLURM_ARRAY_TASK_ID*ZIPS_PER_NODE))

while [ $CURRENT -lt $PROCESSES_PER_NODE ]; do
        START=$((CURRENT*CHUNK_SIZE+MAIN_START))
        STOP=$((START+CHUNK_SIZE))
        OCMD="$CMD $LOCAL_ZIP_FILE $JOBROOT/out_data $START $STOP &"
        echo $OCMD >> $COMMAND_FILE
        eval $OCMD
        ((CURRENT++))
done;

#parallel --linebuffer --progress --jobs $PROCESSES_PER_NODE < $COMMAND_FILE
wait
