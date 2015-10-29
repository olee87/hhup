#!/bin/bash

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LOGDIR="$SCRIPTDIR/logs"
PIDFILE="$LOGDIR/hhup.pid"
LOGFILE="$LOGDIR/hhup.out"

# create log dir if non-existent
[ -e "$LOGDIR" ] || mkdir "$LOGDIR"

COMMAND="${1}"

cd $SCRIPTDIR

FIXPARAMS=-Dhhup.config=config/config.json

case "${COMMAND}" in
	"start" | "-start")
		# checkAlreadyRunning "$FIXPARAMS" "HHUP application"
		echo `pwd`
		echo "checking if $PIDFILE exists"
		if [ -e "$PIDFILE" ]; then
			oldpid=$(cat $PIDFILE)
			echo "pid file $PIDFILE exists, cannot start another instance. stop process with pid $oldpid first"
		else
			echo "no pid file found, starting hhup application"
			nohup java -jar "$FIXPARAMS" ${project.artifactId}-${project.version}.jar >> $LOGFILE 2>&1 &
			pid=$!
			sleep 1
			echo "started hhup application with pid $pid"
			echo $pid > $PIDFILE
		fi
		;;
		
	"stop" | "-stop")
		if [ -e "$PIDFILE" ]; then
			pid=$(cat $PIDFILE)
			echo "stopping process with pid $pid"
			kill $pid
			rm $PIDFILE
		fi
		;;

	kill)
		if [ -e "$PIDFILE" ]; then
			pid=$(cat $PIDFILE)
			echo "killing process with pid $pid"
			pkill -9 $pid
			rm $PIDFILE
		fi
		;;

	*)
		echo "Usage: $0 (start|stop|kill)"
		;;
		
esac
