package com.afivd.afivd;

import java.util.Locale;

// TODO: Currently in progress
public class ResultsToWebView {
    private ParsedResults results;
    private final String documentStart = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "  <meta charset=\"UTF-8\">\n" +
            "</head>\n" +
            "<body>";
    private final String documentEnd = "</body>\n" +
            "</html>\n";
    private final String div = "<div>";
    private final String divEnd = "</div>";

    public ResultsToWebView(ParsedResults results){
        this.results = results;
    }

    private String returnHighlight(String patternName){
        switch (patternName.toLowerCase(Locale.ROOT)){
            case "branch":
                return "#FFDC74"; // neon yellow
            case "constant_coding":
                return "#FBAC87"; // neon orange
            /*
            #FF8C87 neon red
            #F3A6C8 pink
            #DEACF9 purple
            #AEB5FF darker blue
            #95C8F3 blue
            #81E3E1 greenish blue
            #7DE198 green
            #B3E569 light green

             */
            default:
                return "#FFFFFF"; // Default white
        }
    }

}