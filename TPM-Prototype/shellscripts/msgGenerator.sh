FILE="/tmp/server"
function genMessage {
	RAND1=$(($RANDOM * $RANDOM))
	RAND2=$(($RANDOM * $RANDOM))	
	RAND3=$(($RANDOM * $RANDOM))		
	MAC=BCAEC52A90$(($RANDOM % 10))
	case $1 in
	
		#"01") echo "$1:BCAEC52A90C2:0xDEADBEEF$RAND1:${RAND2}ENCRYPTED_STUFF${RAND2}:0xC0FEEAFFE${RAND3}"
		"01") 
			#      PRIO:    MAC     :   DH_KEY Sa    : ENCRYPTED AES STUFF (not given to userspace)
		        #echo   "$1:BCAEC52A90C2:0xDEADBEEF$RAND1:${RAND2}ENCRYPTED_STUFF${RAND2}"
			#      PRIO:    MAC     :   DH_KEY Sa    
			echo    "$1::c::$MAC::0xDEADBEEF$RAND1"
		;;
		"02") echo "$1::c::$MAC::ENCRYPTED_QUOTE$RAND1$RAND2$RAND3::SML_HASH$RAND3"
		;;
		"03") echo "$1::c::$MAC::ENCRYPTED_R_KEY_BY_Sab"
		;;
		*) exit
		;;
	esac
	return $RESULT
}


if [ ! -p $FILE ]; then
	echo "create" $FILE
	mkfifo $FILE
else 
	echo $FILE "already exists"
fi 

while true; do 
	RAND="0"$(($RANDOM % 3 +1))	
	MSG=$(genMessage $RAND)
	echo "writing ["$MSG"] to " $FILE
	echo $MSG > $FILE;
	sleep 0.2
done


