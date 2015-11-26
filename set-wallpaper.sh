#!/bin/bash
cd ${0%/*}
BGD=$(date +%H%M%S|perl -paF -e 's/.(.).(.).(.)/#FF\1\1\2\2\3\3/')
DEC=$(date +%S|perl -ne '$a=$_*4.33; printf("#FF%02x%02x%02x",$a,(64+$a) % 256, (128+$a) % 256)')
TSTP=$(date +%Y-%m-%d.%H:%M:%S)
COUNT=$(adb devices | grep device\$ | wc -l|awk '{print $1}')
ix=1
for i in $( adb devices|grep device\$|cut -f1 ); do
  adb -s $i install -r wallp.apk
  MODEL=Phone:$(adb -s $i shell getprop ro.product.model | tr -dc '!-~')
  IP=IP:$(adb -s $i shell ip addr | awk '/global/{print $2}' | cut -d/ -f1 )
  LNG=$(adb -s $i shell getprop persist.sys.language | tr -dc '!-~')
  COUNTRY=$(adb -s $i shell getprop persist.sys.country | tr -dc '!-~')
  LOC=Locale:$LNG-$COUNTRY
  adb -s $i shell am start \
      -a android.intent.action.MAIN  \
      -c android.intent.category.LAUNCHER \
      -n uk.org.baverstock.wallp/.MainActivity \
      -d "http://$BGD/$DEC/$(hostname -s)/$ix-of-$COUNT/$MODEL/$LOC/$IP/$i/$TSTP"
  ix=$[ $ix + 1 ]
done
