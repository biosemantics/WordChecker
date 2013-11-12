WordChecker
===========

Word Checker

To run GUI 

> java -jar WordChecker.jar 


Quick manual

On the popup
- Select *.djvu (djvu document file converted from original document, such as MSWord or PDF...)
- Select *.docx
- Press Load button

djvu file is used for displaying pages of original document as a reference.
This program extracts all paragraphs and page information from docx file and display in the grid.

If there's existing projects, click the project you want to continue and press continue button,
If there's no, click new button.

Once, main window (with an empty grid) showed up, press start button to parse the document.
Importing/Adding words to dictionary can be possible.


Prepare dictionary from external file

*.dic file is a dictionary file (in plain/text format).
With "import words" button in GUI, you can import these dictionaries to your database.
The database will be located in the same directory and is persistent.

