# git-annex-gallery: A Gallery Generator built to learn Clojure

## Installation

FIXME: Write stuff here!

## Where To Put Your Images

Pictures should be stored beneath a git-annex routeconforming to the
following directory structure.

    ./2011/04/28/Tokyo Trip/Picture3425.jpg

The first three levels of directory outside of the <datadir> are the date
in reverse order. The next "Tokyo Trip" is the name of the specific event.
These combined should give enough information to supply a title and a date
of a set of pictures, which might be enough for lots of people.

## Describing What The Set Of Pictures Is About

Should you wish to describe what the set if pictures is about in detail you
can include a README within the event directory. It should be formatted
using Markdown.

    <datadir>/2011/04/28/Tokyo Trip/README.md

## Describing A Specific Picture

If you want to add a description of what is in the individual picture
create a markdown document with the same name but with a md (markdown)
extension

    <datadir>/2011/04/28/Tokyo Trip/Picture3425.md

## Photo Editing

It may be that you want to keep an originals of all images for posterity
but also want to touch up and experiment with photo editing. To do this
append a .edited to the end of the name component of the filename as shown
below.

    <datadir>/2011/04/28/Tokyo Trip/Picture3425.edited.jpg

I may be able to use git hooks to perform this trickery... so if you edit Picture3425.jpg and save it, git-annex should commit it and perhaps in a git pre-commit hook I can rename and recover the original... that may or may not be possible...

## Usage

To generate the gallery run the following:

    $ java -jar git-annex-gallery-0.1.0-standalone.jar

To install the git / git-annex hooks you will need to run like this:

    $ java -jar git-annex-gallery-0.1.0-standalone.jar annex-monitor
