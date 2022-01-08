Jeanine comments
================
Jeanine comments specify how to layout the source text. They start with a
jeanine: prefix, followed by the directive character and a colon (like p:),
followed by properties (like i:1;), optionally followed by a colon and
another directive with properties and so on. There may only be maximum one
jeanine comment on a single line.

Currently, both directives and property names are a single character in length.
Property values can be of any length, but they cannot include a semicolon.

Currently C-style block comments are always used.

Examples:

/*jeanine:p:i:1;p:0;a:b;x:0;y:30;*/
  jeanine:                          standard jeanine comment prefix
          p:                        "panel" directive
            i:1;p:0;a:b;x:0;y:30;   properties and their values

/*jeanine:s:a:b;i:2;:s:a:b;i:3;*/
  jeanine:                      standard jeanine comment prefix
          s:                    "secondary link" directive
            a:b;i:2;            properties and their values
                    :           directive separator
                     s:         "secondary link" directive
                       a:b;i:3; properties and their values



| Panel directive (p)
| -------------------
| These define a "panel", which is a section of text. Everything from the start
| of a panel directive until the next panel directive (or EOF) will be put in
| the same panel. Panel directives should be placed on a dedicated source line.
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
| |      location as determined by the anchor
| | - y: the y-offset where this panel is located, relatively to the standard
| |      location as determined by the anchor



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