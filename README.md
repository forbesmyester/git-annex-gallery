# git-annex-gallery: A Gallery Generator built to learn Clojure

## About

Outputs distributed gallery built from static HTML / CSS / Thumbnails nailed together using [Git Annex](https://git-annex.branchable.com/).

Git Annex is an awesome project which allows storage of (large) binary objects in git by using symlinks, a seperate data store and clever use of things like SSH and RSYNC and AWS services to move, copy and back up your data onto multiple locations. I believe the use of this technology along with static HTML / CSS / JS generation initiated by git-hooks will lead to a very effective, secure, fault tolerant solution to storing pictures, and perhaps other types of files in future.

## Installation

FIXME: Write stuff here!

## Where To Put Your Images

Pictures should be stored beneath a git-annex route conforming to the
following directory structure.

    ./2011/04/28/Tokyo Trip/Picture3425.jpg

The first three levels of directory outside of the <datadir> are the date
in reverse order. The next "Tokyo Trip" is the name of the specific event.
These combined should give enough information to supply a title and a date
of a set of pictures, which might be enough for lots of people.

Is this the best structure, can we skip the structure and suck all information
from the metadata of the files? What about events that span multiple days,
months or years?

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

It may be able to use git hooks to perform this trickery... so if you edit Picture3425.jpg and save it, git-annex should commit automatically and perhaps in a git pre-commit hook we can instead rename and recover the original... that may or may not be possible...

## Usage

To generate the gallery run the following:

    $ java -jar git-annex-gallery-0.1.0-standalone.jar

To install the git / git-annex hooks you will need to run like this:

    $ java -jar git-annex-gallery-0.1.0-standalone.jar annex-monitor
