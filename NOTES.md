# Notes on Gallery Creation

To install the required command line apps you will need to do the following

    sudo apt-get install imagemagick dcraw

Internally this software will run something like the following when it encounters a RAW formatted file.

    dcraw -c your_full_size_image.rw2 | convert -format png  -thumbnail 320x240 - generated_thumbnail.png

When normal jpeg / png / gif files are encountered it will run the following command

    convert -format png  -thumbnail 320x240 your_full_size_image.jpg generated_thumbnail.png

## Possible components.

 * Identify albums.
 * Identify images within an album.
 * Generate a hash for an image, which will be used to not continually re-create thumbnails that already exist.
 * Generate missing thumbnails for an image.
 * Extract metadata from an image including reading image Markdown.
 * Extract metadata for an album including reading album Markdown.
 * Generate a data file which includes information about the albums.
 * Generate a data file which describes all pictures within an album.
 * Generate some HTML / JS / Clojurescript which can read the data files and allow listing / selection of an albums and pictures with possible next / previous controls.
 * Figure out what git hooks can be leveraged to make this suitable for non technical users and how to script creation of hooks.
 * I think settings should be able to be stored in git, how to script setup and allow modification / reading?
 * Can we make a node-webkit app to wrap the entire app?
 * Can the code be embedded within an Android app?