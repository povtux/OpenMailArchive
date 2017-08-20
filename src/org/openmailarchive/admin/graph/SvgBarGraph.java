package org.openmailarchive.admin.graph;

import org.apache.cxf.common.util.SortedArraySet;

import java.util.HashMap;
import java.util.SortedSet;

public class SvgBarGraph {
    HashMap<String, Integer> values;
    int width;
    int height;
    int barWidth;
    int interBarWidth;
    String title;
    int indicationInterval;

    public String getSvgGraph() {
        String graph = "<svg width=\"" + width + "\" height=\"" + height + "\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n";

        // External border of the graphic
        graph += "<rect x=\"5\" y=\"5\" width=\"" + (width - 10) + "\" height=\"" + (height - 10) + "\" fill=\"white\" stroke=\"black\" stroke-width=\"1\" stroke-opacity=\"1\" shape-rendering=\"crispEdges\" />\n";

        // zone de graph
        graph += "<rect x=\"52\" y=\"50\" width=\"" + (width - 87) + "\" height=\"" + (height - 85) + "\" fill=\"lightgray\" stroke=\"black\" stroke-width=\"1\" stroke-opacity=\"1\" shape-rendering=\"crispEdges\" />\n";

        // Main title
        graph += "<text x=\"" + (width / 2) + "\" y=\"20\" style=\"font-family: sans-serif; font-weight: bold; font-size: 10px;\" text-anchor=\"middle\">" + title + "</text>\n";

        // axes et indications

        // 100% et 50% du max
        graph += "<rect x=\"55\" y=\"80\" width=\"" + (width - 100) + "\" height=\"1\" fill=\"darkgray\" />\n";
        graph += "<rect x=\"55\" y=\"" + (height / 2 + 10) + "\" width=\"" + (width - 100) + "\" height=\"1\" fill=\"darkgray\" />\n";

        // axes
        graph += "<rect x=\"60\" y=\"60\" width=\"2\" height=\"" + (height - 100) + "\" fill=\"black\" />\n";
        graph += "<rect x=\"55\" y=\"" + (height - 45) + "\" width=\"" + (width - 100) + "\" height=\"2\" fill=\"black\" />\n";

        // indications de temps
        //graph += "<rect x=\"112\" y=\"250\" width=\"2\" height=\"5\" fill=\"black\" />";
        //graph += "<text x=\"112\" y=\"265\" style=\"font-family: sans-serif; font-weight: bold; font-size: 10px;\" text-anchor=\"middle\">12h</text>";

        // détermination du max
        int max = 0;
        int res = 0;
        for (String date : values.keySet()) {
            res = values.get(date);
            if (res > max) max = res;
        }


        // indication du max
        graph += "<text x=\"40\" y=\"85\" style=\"font-family: sans-serif; font-weight: bold; font-size: 10px;\" text-anchor=\"middle\">" + max + "</text>\n";
        graph += "<text x=\"40\" y=\"" + (height / 2 + 15) + "\" style=\"font-family: sans-serif; font-weight: bold; font-size: 10px;\" text-anchor=\"middle\">" + (max / 2) + "</text>\n";

        // création des barres de graph
        // 168 valeurs, 3px de large/barre + 1px d'écartement
        // valeur 0 en Y en bas du graph donc à y=250
        // valeur max en Y en haut à 80 dont la valeur est la variable max
        // donc max représente 250-80=170px de haut
        int x = 64;
        int y = 0;
        int barheight = 0;
        int compteur = 0;
        int labelY;
        SortedSet<String> sortedKeys = new SortedArraySet<>();
        sortedKeys.addAll(values.keySet());
        for (String date : sortedKeys) {
            res = values.get(date);

            barheight = res * (height - 130) / max;
            y = (height - 50) - barheight;
            graph += "<rect x=\"" + x + "\" y=\"" + y + "\" width=\"" + barWidth + "\" height=\"" + barheight + "\" fill=\"red\" />\n";

            if (compteur % indicationInterval == 0) {
                if (compteur % (indicationInterval * 2) == 0) labelY = height - 20;
                else labelY = height - 27;
                graph += "<rect x=\"" + (x + (barWidth + interBarWidth) / 2) + "\" y=\"" + (height - 47) + "\" width=\"2\" height=\"6\" fill=\"black\" />\n";
                graph += "<text x=\"" + (x + (barWidth + interBarWidth) / 2) + "\" y=\"" + labelY + "\" style=\"font-family: sans-serif; font-weight: bold; font-size: 7px;\" text-anchor=\"middle\">" + date + "</text>\n";
            }

            compteur++;
            x += barWidth + interBarWidth;
        }

        graph += "</svg>\n";
        return graph;
    }

    public HashMap<String, Integer> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Integer> values) {
        this.values = values;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if (width < 150) this.width = 150;
        else this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (height < 150) this.height = 150;
        else this.height = height;
    }

    public int getBarWidth() {
        return barWidth;
    }

    public void setBarWidth(int barWidth) {
        this.barWidth = barWidth;
    }

    public int getInterBarWidth() {
        return interBarWidth;
    }

    public void setInterBarWidth(int interBarWidth) {
        this.interBarWidth = interBarWidth;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIndicationInterval() {
        return indicationInterval;
    }

    public void setIndicationInterval(int indicationInterval) {
        this.indicationInterval = indicationInterval;
    }
}
