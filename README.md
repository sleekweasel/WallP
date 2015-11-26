# WallP
Appears on the 'Set as' menu. Just sets the wallpaper, but allows the image to shrink smaller than the screen (unlike most wallpaper things I've found).

Multitouch lets you twist it or impose perspective.

Opening an intent as follows sets the wallpaper to the given text:

    adb shell am start -a android.intent.action.MAIN  \
        -c android.intent.category.LAUNCHER \
        -n uk.org.baverstock.wallp/.MainActivity \
        -d http://some/text/you/want/as/a/backdrop

If the first element of the text is #aarrggbb/ that sets the background colour.
If the second element of the text is #aarrggbb/ that sets the decoration colour (the stripes).
If the third element of the text is #aarrggbb/ that sets the text colour.

There is a menu to set text manually, but it doesn't yet allow multi-line and you can't get to it unless you have a menu button. And you have to select a picture anyway to get a canvas to draw on. This is mainly for my debugging.
