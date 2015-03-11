# Notes on Gallery Creation

## Convert RW2 to tiff

    sudo apt-get install ufraw imagemagick dcraw
    ufraw-batch  --output=P1010629.ppm P1010629.RW2
    convert -format png  -thumbnail 320x240 P1010629.ppm  x.png
    dcraw -c P1010629.RW2 | convert -format png  -thumbnail 320x240  - z.png
