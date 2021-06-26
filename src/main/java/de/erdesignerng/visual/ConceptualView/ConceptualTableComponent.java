package de.erdesignerng.visual.ConceptualView;

import de.erdesignerng.model.*;
import de.erdesignerng.visual.IconFactory;
import de.erdesignerng.visual.java2d.BaseRendererComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

public class ConceptualTableComponent extends BaseRendererComponent {
    private final Table table;
    private boolean fullMode;
    private boolean showSelfReference;

    public ConceptualTableComponent(Table aTable) {
        table = aTable;
        initFlags();
    }

    public ConceptualTableComponent(Table aTable, boolean aFullmode) {
        table = aTable;
        fullMode = aFullmode;
        initFlags();
    }

    private void initFlags() {
        showSelfReference = false;
        for (Relation theRelation : table.getOwner().getRelations().getForeignKeysFor(table)) {
            if (theRelation.isSelfReference()) {
                showSelfReference = true;
            }
        }
    }

    @Override
    public Dimension getSize() {
        Dimension theSize = new Dimension(0, 0);
        FontMetrics theMetrics = getFontMetrics(getFont());

        Rectangle2D theStringSize = theMetrics.getStringBounds(table.getName(), null);
        theSize = update(theSize, (int) theStringSize.getWidth(), theMetrics.getAscent());

        for (Attribute<Table> theAttriute : table.getAttributes()) {
            boolean theInclude = true;
            if (!fullMode) {
                theInclude = theAttriute.isForeignKey() || !theAttriute.isNullable();
            }
            if (theInclude) {
                String theText = theAttriute.getName();
                if (fullMode) {
                    theText += ":";
                    theText += theAttriute.getLogicalDeclaration();
                }
                theStringSize = theMetrics.getStringBounds(theText, null);
                theSize = update(theSize, (int) theStringSize.getWidth(), theMetrics.getAscent());
            }
        }
        if (table.getIndexes().size() > 0 && fullMode) {
            for (Index theIndex : table.getIndexes()) {
                if (theIndex.getIndexType() != IndexType.PRIMARYKEY) {
                    String theName = theIndex.getName();
                    theStringSize = theMetrics.getStringBounds(theName, null);
                    theSize = update(theSize, (int) theStringSize.getWidth(), theMetrics.getAscent());
                    for (IndexExpression theExpression : theIndex.getExpressions()) {
                        theName = theExpression.toString();
                        theStringSize = theMetrics.getStringBounds(theName, null);
                        theSize = update(theSize, (int) theStringSize.getWidth() + 20, theMetrics.getAscent());
                    }
                }
            }
        }


        theSize.width += 20;
        if (fullMode) {
            theSize.width += 10;
        }

        theSize.height += 25;

        return theSize;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D theGraphics = (Graphics2D) g;
        Dimension theSize = getSize();
        FontMetrics theMetrics = getFontMetrics(getFont());

        theGraphics.setColor(Color.decode("#F29492"));

        theGraphics.drawRect(10, 10, theSize.width - 10, theSize.height - 10);
        theGraphics.drawRect(10, 10, theSize.width - 10, 10 + theMetrics.getAscent());

        GradientPaint thePaint = new GradientPaint(0, 0, Color.decode("#F29492"), theSize.width - 35, theSize.height,
                Color.decode("#114357"), false);
        theGraphics.setPaint(thePaint);
        theGraphics.fillRect(11, 11, theSize.width - 10 - 1, 10 + theMetrics.getAscent() - 1);

        thePaint = new GradientPaint(0, 0, new Color(90, 90, 90), theSize.width - 35, theSize.height,
                Color.decode("#114357"), false);
        theGraphics.setPaint(thePaint);
        theGraphics.fillRect(11, 19 + theMetrics.getAscent(), theSize.width - 10 - 1, theSize.height - 32);

        theGraphics.setColor(Color.white);

        theGraphics.drawString(table.getName(), 15, 10 + theMetrics.getAscent());

        int y = 18 + theMetrics.getAscent();

        for (Attribute<Table> theAttriute : table.getAttributes()) {

            g.setColor(Color.white);

            boolean theInclude = true;
            if (!fullMode) {
                theInclude = theAttriute.isForeignKey() || !theAttriute.isNullable();
            }

            if (showSelfReference) {
                ImageIcon theIcon = IconFactory.getSelfReferenceIcon();
                int xp = theSize.width - theIcon.getIconWidth() - 4;
                int yp = 14;

                theIcon.paintIcon(this, theGraphics, xp, yp);
            }
        }
        if (table.getIndexes().size() > 0 && fullMode) {
            boolean lineDrawn = false;
            for (Index theIndex : table.getIndexes()) {
                if (theIndex.getIndexType() != IndexType.PRIMARYKEY) {
                    if (!lineDrawn) {
                        y += 3;
                        theGraphics.setColor(Color.blue);
                        theGraphics.drawLine(10, y, theSize.width, y);
                        lineDrawn = true;
                    }
                    String theName = theIndex.getName();
                    theGraphics.setColor(Color.white);
                    theGraphics.drawString(theName, 15, y + theMetrics.getAscent());
                    y += theMetrics.getAscent();
                    for (IndexExpression theExpression : theIndex.getExpressions()) {
                        theName = theExpression.toString();
                        theGraphics.drawString(theName, 20, y + theMetrics.getAscent());
                        y += theMetrics.getAscent();
                    }
                }
            }
        }
    }
}
