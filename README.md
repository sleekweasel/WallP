# WallP
Appears on the 'Set as' menu. Just sets wallpaper, but allows the image
to shrink smaller than the screen (unlike most wallpaper things I've found).

Multitouch lets you twist it or impose perspective.

Opening an intent as follows sets the wallpaper to the given text:

adb shell am start -a android.intent.action.MAIN  -c android.intent.category.LAUNCHER \
   -d http://some/text/you/want/as/a/backdrop  -n uk.org.baverstock.wallp/.MainActivity
