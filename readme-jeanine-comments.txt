Jeanine comments
================
Jeanine comments specify how to layout the source text. These are simply
C-style block comments that start with a 'jeanine:' prefix.
There may only be maximum one jeanine comment on a single line.
The comment may be on its own line or at the end of a non-empty line but
certain directives require specific placement, this will be mentioned
throughout this document.

After the prefix (jeanine:) follows one or more directives, which are separated
by a colon character (:).

A directive is a single character, followed by a colon (:), optionally followed
by one or more properties.

A directive property is a single character, followed by a colon (:), followed
by its value, and ends with a semicolon (;)

If a property value is supposed to be a float, it must include a decimal point.

String property values cannot contain the semicolon character (;) as that marks
the end of a property (value). It also cannot contain the sequence '*/' as that
(early) marks the end of a (jeanine) comment. Any other character (apart from
the obvious line break characters) is permitted.

Examples:

/*jeanine:p:i:1;p:0;a:b;x:0;y:30;*/
  jeanine:                          standard jeanine comment prefix
          p:                        "panel" directive
            i:1;p:0;a:b;x:0;y:30;   properties and their values

/*jeanine:s:a:b;i:2;:s:a:b;i:3;*/
  jeanine:                        standard jeanine comment prefix
          s:                      "secondary link" directive
            a:b;i:2;              properties and their values
                    :             directive separator
                     s:           "secondary link" directive
                       a:b;i:3;   properties and their values

/*jeanine:x::x:y:z;:x:*/
  jeanine:               standard jeanine comment prefix
          x:             'x' directive (does not exist, just an example)
            :            directive separator (meaning the previous x directive
                           has no properties)
             x:          another 'x' directive
               y:z;      properties and their values
                   :     directive separator
                    x:   another 'x' directive


| Panel directive (p)
| -------------------
| These define a "panel", which is a section of text. Everything from the start
| of a panel directive until the next panel directive (or EOF) will be put in
| the same panel. Panel directives must be placed on a dedicated source line.
| The start of the source implicitely defines the root panel, which has an id
| of 0. The root panel is the only panel that has no parent.
| 
|
| | Properties
| | ----------
| | - a: the anchor which specifies how this panel is attached to its parent:
| |      - b: bottom (bottom of parent linked to top of child)
| |      - t: top (top of parent linked to top of child)
| |      - r: right (requires a right link location directive; see below)
| | - i: the id of this panel
| | - p: the id of the parent of this panel
| | - x: the x-offset where this panel is located, relatively to the standard
| |      location as determined by the anchor. If this is a float value,
| |      it is a multiple of the font width, otherwise it's in pixels.
| | - y: the y-offset where this panel is located, relatively to the standard
| |      location as determined by the anchor. If this is a float value,
| |      it is a multiple of the font height, otherwise it's in pixels.
| | - n: the name of the panel



| Right link directive (r)
| ------------------------
| These define the location where panels with a 'right' anchor are linked.
| Since a right link is linked at a specific line, it needs an additional
| jeanine directive to know at which line it is linked (unlike top and bottom
| links, which are always at panel boundaries).
|
| Using a property in the panel directory to store the line number where the
| link is located would be less suitable, since it will possibly be incorrect
| after making edits in the source while not in jeanine/2d mode. Putting a
| jeanine comment at the end of the line that is linked, will survive those
| edits.
|
|
| | Properties
| | ----------
| | - i: the id of the child panel that is linked from here



| Secondary link directive (s)
| ----------------------------
| While panels are already linked by means of properties in the panel directive,
| secondary links can also be made so it is possible to have multiple links to
| the same panel. The difference between primary and secondary links is that
| a primary link defines the location of the child. A panel must always have a
| primary link (except for the root panel). Secondary links are outgoing,
| meaning they are placed at the parent's position and link to a child.
|
| Secondary links with a top or bottom anchor can be placed in a jeanine comment
| anywhere within the panel's region, but are usually put at the end. They must
| be in a jeanine comment that has its own dedicated line. That comment may then
| not contain any directives that don't require a dedicated line (like a
| secondary link with a right anchor).
|
| Secondary links with a right anchor must be placed at the end of the line
| where the link should be.
|
|
| | Properties
| | ----------
| | - i: the id of the panel that is linked here
| | - a: anchor:
| |      - b: bottom (bottom of parent linked to top of child)
| |      - t: top (top of parent linked to top of child)
| |      - r: right (this line at the parent to top of child)



| Legacy right link directive (l)
| -------------------------------
|
| | THIS DIRECTIVE IS LEGACY AND DOES NOT FOLLOW THE PROPER DIRECTIVE SYNTAX.
| | Implementations may parse these directive but are not allowed to produce them.
| | When parsed, they should be converted to right link directives (r).
|
| These were in use when jeanine comments weren't fully specced out yet. They
| are deprecated and won't newly appear any more, but might still be interpreted
| for backward compatibility reasons. The 'l' initially stood for 'link'. This
| directive is the functional equivalent of 1..n right link directives (r).
|
| This directive doesn't have properties, but rather a comma separated list of
| ids that denote the child ids. (This inconsistency is the reason that it is
| deprecated)
|
|
| | Syntax
| | ------
| | /*jeanine:l:2,1*/
| |   jeanine:        standard jeanine comment prefix
| |           l:      "link" directive
| |             2,1   child panel ids that are right-linked here (2 and 1)
