import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;


/* Looking back, what should have been made different? <br>
 * - Instead of implementing the pattern via Strings (pattern-names), own Object builders for each pattern would have been clearer. <br>
 * For example: {@code Bitmap.Pattern grid = Bitmap.Patterns.Grid(<args>)} <br>
 * - Subclass ColorProvider instead of checking the Object argument each time. <br>
 * - The IllegalArgumentExceptions are informative, but not if one needs to catch a specific one. Own Exceptions and a more diverse use of them. <br>
 * - Outsource some of the code (for example the classes), to make the code less cluttered. <br>
 * - Overloads are pretty annoying especially on a large scale, find alternative way. <br>
 * - <s>Gradients should be able to scale automatically, declaring from and to each time can be a hassle.</s> Implemented <br>
 * - Uniform naming, <s> Object argument should always be called color_provider (instead of color) </s> <br>
 * - <s>Pattern should not be too powerful, outsource the fast pattern handler</s> I don't think that pattern is a god object, or too powerfull<br>
 * - <b>Created pixels should be easy to delete</b> <br>
 */


/** Java class to easily create and manipulate .ppm files. <br>
 * Created by Simon Conrad © 2021 <br>
 * <a href=https://github.com/IsAvaible/Bitmap>Github</a> <br>
 */
public class Bitmap {

    public final int canvas_width; // The width of the canvas in pixel
    public final int canvas_height; // The height of the canvas in pixel
    public Colors colors = new Colors(); // A object of the colors subclass
    public Shapes shapes = new Shapes(); // A object of the shapes subclass

    private String filename; // The filename that is used in the render method
    private final int[][][] canvas; // A three dimensional representation of the array columns[rows[pixel[]]]

    /** Creates a new Bitmap object
     * d = default (can be left away)
     * @param canvas_width The width of the canvas in pixel
     * @param canvas_height The height of the canvas in pixel
     * @param filename The name of the file that is created upon execution (must be .ppm or none) | d = Bitmap.ppm
     * @param render_on_init Whether the canvas should be rendered upon execution (file is created / overwritten) | d = true
     */
    public Bitmap(int canvas_width, int canvas_height, String filename, boolean render_on_init) {
        this.canvas_width = canvas_width;
        this.canvas_height = canvas_height;
        canvas = new int[canvas_height][canvas_width][3];
        this.filename = filename;
        if (render_on_init) render(true);
    }

    /**@see #Bitmap(int, int, String, boolean) **/
    public Bitmap(int canvas_width, int canvas_height, String filename) { this(canvas_width, canvas_height, filename, false); }
    /**@see #Bitmap(int, int, String, boolean) **/
    public Bitmap(int canvas_width, int canvas_height) { this(canvas_width, canvas_height, true); }
    /**@see #Bitmap(int, int, String, boolean) **/
    public Bitmap(int canvas_width, int canvas_height, boolean render_on_init) { this(canvas_width, canvas_height, "Bitmap.ppm", render_on_init); }

    public int[][][] getCanvas() {
        return canvas;
    }

    /** Validates the syntax of a color
     * @param color The color that should be validated
     * @throws IllegalArgumentException when the color doesn't match criteria
     */
    private void validateColor(Color color) {
        if (color.color.length != 3) {
            throw new IllegalArgumentException("color has the wrong length");
        }

        for (int color_information : color.color) {
            if (color_information < 0 || color_information > 255) {
                throw new IllegalArgumentException("at least one rgb value is too big");
            }
        }

    }

    /** Changes exactly one pixel at a specified point
     * @param x coordinate of the addressed pixel
     * @param y coordinate of the addressed pixel
     * @param color_provider a object of type Color or Pattern that provides the color
     * @throws IllegalArgumentException when the color_provider isn't a Pattern or a Color
     * @throws Exceptions.PixelOutOfBoundsException when the accessed pixel is outside the canvas
     */
    private void changePixel(int x, int y, Object color_provider)  {
        if (x < 1 || x > canvas_width) {
            throw new Exceptions.PixelOutOfBoundsException("x is out of bounds");
        } else if (y < 1 || y > canvas_height) {
            throw new Exceptions.PixelOutOfBoundsException("y is out of bounds");
        } else {}

        if (color_provider.getClass() == Color.class) {
            canvas[canvas_height -y][x-1] = ((Color) color_provider).color;
        } else if (color_provider.getClass() == Pattern.class) {
            canvas[canvas_height -y][x-1] = ((Pattern) color_provider).run(x, y).color;
        } else {
            throw new IllegalArgumentException("How did you even get here? color_provider can be only be a Pattern or a Color");
        }
    }

    /**@see #changePixel(int, int, Object) **/
    public void changePixel(int x, int y, Color color) {
        changePixel(x, y, (Object) color);
    }

    /**@see #changePixel(int, int, Object) **/
    public void changePixel(int x, int y, Pattern pattern) {
        changePixel(x, y, (Object) pattern);
    }


    /** Validate if the type of the object is correct
     * @param obj Object to validate. Must be either Color or Pattern.
     * @throws IllegalArgumentException if the object isn't a color or a pattern
     */
    private void checkType(Object obj) {
        if (obj.getClass() != Color.class && obj.getClass() != Pattern.class) {
            throw new IllegalArgumentException("color_provider can only be a Pattern or a Color");
        }
    }

    /**
     * @param x_p1 The x-coordinate of the first point
     * @param y_p1 The y-coordinate of the first point
     * @param x_p2 The x-coordinate of the second point
     * @param y_p2 The y-coordinate of the second point
     * @param color_provider The color provider, has to be of type Color or Pattern
     * @param outline The outline object
     * @see Outline#Outline(boolean, int, Object) 
     * @throws IllegalArgumentException if the color_provider isn't of type Color or pattern
     */
    public void fillArea(int x_p1, int y_p1, int x_p2, int y_p2, Object color_provider, Outline outline) {
        int min_x, max_x, min_y, max_y;
        min_x = Math.min(x_p1, x_p2); max_x = Math.max(x_p1, x_p2);
        min_y = Math.min(y_p1, y_p2); max_y = Math.max(y_p1, y_p2);

        checkType(color_provider);
        if (color_provider.getClass() == Pattern.class) {
            setAutoPattern(min_x, max_x, min_y, max_y, color_provider, false);
        }

        if (outline.active) {
            border(min_x, min_y, max_x, max_y, outline.thickness, outline.color);
        }

        for (int y = min_y; y <= max_y; y++) {
            for (int x = min_x; x <= max_x; x++) {
                changePixel(x, y, color_provider);
            }
        }
    }

    /** Creates a horizontal line between two x-coordinate
     * @param x_from The first x-coordinate
     * @param x_to The second x-coordinate
     * @param y_pos The y-coordinate of the line
     * @param color_provider The color provider, has to be either Color or Pattern
     * @param thickness The thickness of the line (upwards)
     */
    public void lineH(int x_from, int x_to, int y_pos, Object color_provider, int thickness) {
        fillArea(x_from, y_pos, x_to, y_pos+thickness-1, color_provider);
    }

    /** Creates a vertical line between two x-coordinate
     * @param y_from The first y-coordinate
     * @param y_to The second y-coordinate
     * @param x_pos The x-coordinate of the line
     * @param color_provider The color provider, has to be either Color or Pattern
     * @param thickness The thickness of the line (to the right)
     */
    public void lineV(int y_from, int y_to, int x_pos, Object color_provider, int thickness) {
        fillArea(x_pos, y_from, x_pos+thickness-1,  y_to, color_provider);
    }

    /**
     * Clears the bitmap (this) by filling it with black
     */
    public void clear() {
        fillWin();
    }

    /** Creates a border in a specified area (outwards facing)
     * @param x_p1 The x-coordinate of the first point
     * @param y_p1 The y-coordinate of the first point
     * @param x_p2 The x-coordinate of the second point
     * @param y_p2 The y-coordinate of the second point
     * @param thickness The thickness of the border (outwards)
     * @param color_provider The color provider, has to be either Color or Pattern
     */
    public void border (int x_p1, int y_p1, int x_p2, int y_p2, int thickness, Object color_provider) {
        checkType(color_provider);

        int min_x, max_x, min_y, max_y;
        min_x = Math.min(x_p1, x_p2); max_x = Math.max(x_p1, x_p2);
        min_y = Math.min(y_p1, y_p2); max_y = Math.max(y_p1, y_p2);
        Pattern[] lockedPatterns =  setAutoPattern(min_x, max_x, min_y, max_y, color_provider, true);
        // X-Axis
        fillArea(min_x-thickness, min_y, max_x+thickness,min_y-thickness, color_provider);
        fillArea(min_x-thickness, max_y, max_x+thickness, max_y+thickness, color_provider);
        // Y-Axis
        fillArea(min_x, min_y, min_x-thickness, max_y, color_provider);
        fillArea(max_x, min_y, max_x + thickness, max_y, color_provider);
        // Unlocking the patterns again
        unlockAutoPattern(lockedPatterns);
    }

    /**
     * @param color_provider If cp is a Color, the function is more efficient than fillArea(). Can also be set to a Pattern.
     * @throws IllegalArgumentException if the color provider is not of type Color or Pattern
     */
    public void fillWin(Object color_provider) {
        if (color_provider.getClass() == Color.class) {
            validateColor((Color) color_provider);
            for (int[][] row : canvas) {
                Arrays.fill(row, ((Color) color_provider).color);
            }
        } else if (color_provider.getClass() == Pattern.class) {
            fillArea(1, 1, canvas_width, canvas_height, color_provider);
        } else {
            throw new IllegalArgumentException("color_provider must be of class Color or Pattern");
        }

    }

    /** Sets the from and to on pattern that are labeled as auto recursively
     * @param from_x From when the pattern is horizontal
     * @param to_x To when the pattern is horizontal
     * @param from_y From when the pattern is vertical
     * @param to_y To when the pattern is vertical
     * @param color_provider The color provider, has to be either Color or Pattern
     * @param lockIn Whether the patterns should be set to auto = false upon assignment
     * @return Pattern[] lockedPatterns : A list of all patterns that were locked during execution
     */
    private Pattern[] setAutoPattern(int from_x, int to_x, int from_y, int to_y, Object color_provider, boolean lockIn) {
        // Growable list storing the locked patterns
        ArrayList<Pattern> lockedPatterns = new ArrayList<>();
        if (color_provider.getClass() == Color.class) {
            // No branches here anymore
        } else if (color_provider.getClass() == Pattern.class) {
            // Recursive call of both slots to make sure every branch is visited
            lockedPatterns.addAll(Arrays.asList( setAutoPattern(from_x, to_x, from_y, to_y, ((Pattern) color_provider).slot_1, lockIn) ));
            lockedPatterns.addAll(Arrays.asList( setAutoPattern(from_x, to_x, from_y, to_y, ((Pattern) color_provider).slot_2, lockIn) ));
            // Setting the from and to only when auto mode is selected
            if (((Pattern) color_provider).auto) {
                boolean horizontal = ((Pattern) color_provider).horizontal;
                ((Pattern) color_provider).from = horizontal ? from_y : from_x;
                ((Pattern) color_provider).to = horizontal ? to_y : to_x;
                if (lockIn) {
                    ((Pattern) color_provider).auto = false; // If the pattern should be locked in, auto is set to false
                    lockedPatterns.add((Pattern) color_provider);
                }
            }
        }
        // Converting the list to an array
        Object[] temp = lockedPatterns.toArray();
        // Casting all Objects in the list to Pattern
        return Arrays.copyOf(temp, temp.length, Pattern[].class);
    }

    /** Can be used to unlock patterns previously locked by the setAutoPattern
     * @param patterns an array of patterns that should be unlocked
     */
    private void unlockAutoPattern(Pattern[] patterns) {
        for (Pattern pattern : patterns) {
            pattern.auto = true;
        }
    }

    /** Writes the bitmap to a file
     *  d = default
     * @param filename The name of the file to which the bitmap should be written | d = Bitmap.ppm
     * @param custom_win A three dimensional array containing custom pixel information | d = canvas
     * @param report_path | d false
     * @throws IllegalArgumentException if the specified file is not in the ppm format, or if the write operation wasn't successful
     */
    public void render(String filename, int[][][] custom_win, boolean report_path) {
        this.filename = filename;
        String full_filepath = System.getProperty("user.dir") + "/" + filename;

        try {
            // Creating a new File object
            File bitmap = new File(full_filepath);
            // Checking if the file exists
            if (!bitmap.exists()) {
                // Creating a new file
                if (bitmap.createNewFile()) {
                    System.out.println("created new file");
                }
            }
            // Creating a new file writer
            FileWriter render_obj = new FileWriter(full_filepath);

            // Checking whether the file in the right format
            if (!filename.split("\\.")[1].equals("ppm")) {
                throw new IllegalArgumentException("file is not of the ppm file format");
            }

            // Better way to do build a string in a for loop
            StringBuilder converted_win = new StringBuilder();
            // Adding the header
            converted_win.append(String.format("P3\n#%s\n%s %s\n255\n", filename, canvas_width, canvas_height));

            // Building the file content
            for (int[][] row : custom_win) {
                for (int[] pixel : row) {
                    for (int color_information : pixel) {
                        converted_win.append(color_information).append(" ");
                    }
                }
                converted_win.append("\n");
            }

            if (report_path) { // Output the full filepath
                System.out.println("Writing object to: " + full_filepath);
            }
            // Write the build string to the file
            render_obj.write(converted_win.toString());
            // Close the file
            render_obj.close();

        } catch (FileNotFoundException e) {
            // When the current image viewer reloads the file, it is not accessible to Java.
            // Therefore waiting a small amount of time can ensure cleared file locks.
            // Might also result in a endless loop ¯\_(ツ)_/¯
            try {
                Thread.sleep(50);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            render(filename, custom_win, report_path);

        } catch (IOException e) {
            System.out.println(e);
            throw new IllegalArgumentException("Error: couldn't write to file.");
        }
    }

    /**@see #render(String, int[][][], boolean) **/
    public void render(int[][][] custom_win) {render(filename, custom_win, false);}
    /**@see #render(String, int[][][], boolean) **/
    public void render() {render(filename, canvas, false);}
    /**@see #render(String, int[][][], boolean) **/
    public void render(String filename) {render(filename, canvas, false);}
    /**@see #render(String, int[][][], boolean) **/
    public void render(int[][][] custom_win, String filename) {render(filename, custom_win,false);}
    /**@see #render(String, int[][][], boolean) **/
    public void render(boolean report_path) {render(filename, canvas, report_path);}

    // Stores a color in the RGB color format
    public class Color {
        int[] color;

        /**
         * @param color a int[] representation of the RGB-color
         * @param alpha (brightness) between 0.0 (darkest) and 1.0 (normal)
         */
        Color (int[] color, double alpha) {
            this.color = color;
            if (alpha != 1.0) {
                setAlpha(alpha);
            }
            validateColor(this);
        }

        /**
         * @param alpha (brightness) between 0.0 (darkest) and 1.0 (brightest)
         * @return this
         */
        public Object setAlpha(double alpha) {
            int[] col = this.color.clone();

            if (alpha < 0.0 && alpha > 1.0) {
                throw new IllegalArgumentException("alpha should be a double between 0 and 1");
            }
            for (int i = 0; i < 3; i++) {
                col[i] = (int) (col[i] * alpha);
            }
            this.color = col;
            return this;
        }


        /**
         * @param r red
         * @param g green
         * @param b blue
         * @param alpha (brightness) between 0.0 (darkest) and 1.0 (brightest)
         */
        public Color(int r, int g, int b, double alpha) { this(new int[]{r, g, b}, alpha); }

        /**
         * @param color a string representation of the color in the format "r g b"
         * @param alpha (brightness) between 0.0 (darkest) and 1.0 (brightest)
         */
        Color (String color, double alpha) { this(Stream.of(color.split(" ")).mapToInt(Integer::parseInt).toArray(), alpha); }

        /**@see #Color(int[], double)**/
        public Color(int[] color) {this(color, 1.0);}
        /**@see #Color(int, int, int, double)**/
        Color (int r, int g, int b) {this(r,g,b,1.0);}
        /**@see #Color(String, double)**/
        Color (String color) {this(color,1.0);}
    }

    /** Class to create patterns.
     * Patterns are a mix out of two color providers.
     */
    public class Pattern {

        private Object slot_1;
        private Object slot_2;
        private final String pattern;

        private boolean horizontal;
        private boolean vertical;

        boolean shiftPattern;

        Predicate<int[]> custom_function;

        int from;
        int to;
        boolean auto = false;

        double opacity;

        private Pattern (Object slot_1, Object slot_2, String pattern, boolean horizontal, boolean shiftPattern, int from, int to, boolean fastPattern, Predicate<int[]> custom_function, double opacity) {
            checkType(slot_1);
            checkType(slot_2);
            this.slot_1 = slot_1; this.slot_2 = slot_2;

            this.horizontal = horizontal; this.vertical = !horizontal;
            this.shiftPattern = shiftPattern;
            this.from = from;
            this.to = to;

            this.custom_function = custom_function;
            this.opacity = opacity;

            if (fastPattern) pattern = evaluateFastPattern(pattern);
            validatePattern(pattern);

            this.pattern = pattern;

        }

        /** Calls the first slot recursively or returns the color directly
         * @param x the x-coordinate
         * @param y the y-coordinate
         * @return the color that is returned by the recursive call
         */
        public Color run_slot_1(int x, int y) {
            return slot_1.getClass() == Color.class ? (Color) slot_1 : ((Pattern) slot_1).run(x, y);
        }

        /** Calls the second slot recursively or returns the color directly
         * @param x the x-coordinate
         * @param y the y-coordinate
         * @return the color that is returned by the recursive call
         */
        public Color run_slot_2(int x, int y) {
            return slot_2.getClass() == Color.class ? (Color) slot_2 : ((Pattern) slot_2).run(x, y);
        }

        public void setSlot_1(Object slot_1) { checkType(slot_1); this.slot_1 = slot_1; }
        public void setSlot_2(Object slot_2) { checkType(slot_2); this.slot_2 = slot_2; }
        public Object getSlot_1() { return slot_1; }
        public Object getSlot_2() { return slot_2; }

        /** Calculates the slot that is returned
         * @param x the x-coordinate
         * @param y the y-coordinate
         * @return a Color
         */
        public Color run(int x, int y) {
            if (shiftPattern) x++; y++;
            if (horizontal) {int temp = x; x = y; y = temp;}
            switch (pattern) {
                // Main patterns
                case "normal":
                    return run_slot_1(x, y);
                case "opacity":
                    return colors.mix(run_slot_1(x, y), new Color(vertical ? canvas[canvas_height-y-(shiftPattern?1:0)][x-(shiftPattern?2:1)] : canvas[canvas_height-x-(shiftPattern?1:0)][y-(shiftPattern?2:1)]), opacity);
                case "grid":
                    return x * y % 2 == 0 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "stripes":
                    return x % 2 == 0 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "checkerboard":
                    return (x+y) % 2 == 0 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "gradient":
                    double balance = Math.max(Math.min((x - from) / (double) (to - from), 1.0), 0.0);
                    if (slot_1.getClass() == Pattern.class && slot_2.getClass() == Pattern.class && ((Pattern) slot_1).pattern.equals("gradient") && ((Pattern) slot_2).pattern.equals("gradient")) {
                        return colors.mix(new Color(run_slot_1(x, y).color), new Color(run_slot_2(x, y).color), 0.5); // If both slots are gradients, the colors should be mixed 1:1
                    }
                    return colors.mix(new Color(run_slot_1(x, y).color), new Color(run_slot_2(x, y).color), balance);
                case "cells":
                    return (shiftPattern == ((Math.sin(x * y)) > 0.5)) ? run_slot_1(x, y) : run_slot_2(x, y);
                case "bigcells":
                    return Math.sin(Math.toDegrees(x*y)) > 0.1 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "space":
                    return  (int) Math.toDegrees(Math.sin(x*y))*1.5 % 2 == 0 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "dotgrid":
                    return  x*y % 4 == 0 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "biggrid":
                    return  x*y % 5 == 0 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "hugegrid":
                    return  x*y % 19 == 0 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "superhugegrid":
                    return  x*y % 73 == 0 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "flowergrid":
                    return  x*y % 6 == 0 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "dotlines":
                    return  (int) Math.sin(Math.toRadians(x*y)) % 2 == 0 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "wave":
                    return  Math.sin((double) x/y) > 0.05 ? run_slot_1(x, y) : run_slot_2(x, y);
                case "custom":
                    return  custom_function.test(new int[]{x, y}) ? run_slot_1(x, y) : run_slot_2(x, y);
            }

            throw new IllegalArgumentException(pattern + " is a unknown pattern");
        }

        /** Checks if the pattern is known
         * @param pattern a lowercase String containing the pattern name
         */
        public void validatePattern(String pattern) {
            if (!Set.of("grid", "checkerboard", "stripes", "gradient", "wave", "cells", "bigcells", "dotgrid", "biggrid", "hugegrid", "superhugegrid", "flowergrid", "space", "dotlines", "custom", "opacity", "normal").contains(pattern)) {
                throw new IllegalArgumentException(pattern + " is a unknown pattern");
            }
            if (pattern.equals("gradient")) {
                if (from > to) throw new IllegalArgumentException("from must be smaller than to");
            } else if (pattern.equals("opacity")) {
                if (opacity < 0) throw new IllegalArgumentException("opacity can't be below 0.0");
            }
        }

        /** Parses the provided fast-pattern. <br>
         * A fast pattern must consist of the pattern name e.g. "gradient". <br>
         * It can also contain either "H" (for horizontal) or "V" (for vertical). <br>
         * To shift the pattern, the character ">" can be used. <br>
         * From and to or the opacity can be set by adding a "=" + "from-to" / "opacity". <br>
         * To activate auto mode, add "=auto" at the end of the pattern. <br>
         * For example: {@code evaluateFastPattern("gradientH=auto")}  <br>
         * or: {@code evaluateFastPattern("stripesV>")}  <br>
         * or: {@code evaluateFastPattern("opacity=0.34")}  <br>
         * @param fast_pattern the fast-pattern
         * @return the pattern extracted from the fast-pattern
         */
        public String evaluateFastPattern(String fast_pattern) {
            int pattern_length = -1;
            for (int c = 0; c < fast_pattern.length(); c++) {
                if (fast_pattern.charAt(c) == 'H' || fast_pattern.charAt(c) == 'V') { // Horizontal / Vertical
                    if (pattern_length == -1) pattern_length = c;
                    setHorizontal(fast_pattern.charAt(c) == 'H');
                } else if (fast_pattern.charAt(c) == '>') { // Shift pattern
                    if (pattern_length == -1) pattern_length = c;
                    this.shiftPattern = true;
                } else if (fast_pattern.charAt(c) == '=') { // Set from and to / opacity
                    if (pattern_length == -1) pattern_length = c;
                    if (fast_pattern.substring(c+1).equals("auto")) this.auto = true;
                    else {
                        String[] from_to = fast_pattern.substring(c+1).split("-");
                        try {
                            this.from = Integer.parseInt(from_to[0]);
                            this.to = Integer.parseInt(from_to[1]);
                        } catch (Exception ignored) {
                            try {
                                this.opacity = Double.parseDouble(fast_pattern.substring(c+1));
                            } catch (Exception ignored_2) {
                                throw new IllegalArgumentException("from-to / opacity was not declared properly");
                            }
                        }
                    }
                }
            }

            if (pattern_length == -1) return fast_pattern;
            else return fast_pattern.substring(0, pattern_length);
        }

        public void setHorizontal(boolean horizontal) {
            this.horizontal = horizontal; this.vertical = !horizontal;
        }

        private Pattern (Object slot_1, Object slot_2, String pattern, boolean horizontal, boolean shiftPattern, int from, int to, boolean fastPattern, Predicate<int[]> custom_function) {
            this(slot_1, slot_2, pattern, horizontal, shiftPattern, from, to, fastPattern, custom_function, -1.0);
        }

        private Pattern (Object slot_1, Object slot_2, String pattern, boolean horizontal, boolean shiftPattern, int from, int to, boolean fastPattern) {
            this(slot_1, slot_2, pattern, horizontal, shiftPattern, from, to, fastPattern, null);
        }

        /** Fast pattern overload
         * @param slot_1 The first object
         * @param slot_2 The second object
         * @param fast_pattern To define for example a fast gradient: "gradient{H/V}={from : int}-{to : int}", for example {@code "gradientH=100-200"}
         * @see #evaluateFastPattern(String)
         */
        public Pattern (Object slot_1, Object slot_2, String fast_pattern) {
            this(slot_1, slot_2, fast_pattern, true, false, 0, 0, true);
        }

        /** Opacity pattern
         * @param slot_1 The color provider
         * @param opacity The opacity of the color provider (1.0 = cover, 0.0 = invisible)
         */
        public Pattern(Object slot_1, double opacity) {
            this(slot_1, colors.white(), "opacity", true, false, 0, 0, false, null, opacity);
        }

        /** Custom pattern
         * @param slot_1 The first object
         * @param slot_2 The second object
         * @param custom_function Predicate that accepts a Integer Array. The first element is
         *                        the x-coordinate, the second element is the y-coordinate. <br>
         *                        Example: <br>
         *                        {@code Predicate<int[]> custom_fun = arr -> arr[0] * arr[1] % 16 == 0;}
         *
         */
        public Pattern (Object slot_1, Object slot_2, Predicate<int[]> custom_function) {
            this(slot_1, slot_2, "custom", true, false, 0, 0, true, custom_function);
        }

        /** Pattern overload for patterns that use from and to (gradient)
         * @param slot_1 The first object
         * @param slot_2 The second object
         * @param pattern The name of the pattern
         * @param horizontal Whether the pattern should be horizontal or vertical
         * @param from The first coordinate (int)
         * @param to The second coordinate (int)
         */
        public Pattern (Object slot_1, Object slot_2, String pattern, boolean horizontal, int from, int to) {
            this(slot_1, slot_2, pattern, horizontal, false, from, to, false);
        }

        /** Standard pattern
         * @param slot_1 The first object
         * @param slot_2 The second object
         * @param pattern The name of the pattern
         * @param horizontal Whether the pattern should be horizontal or vertical
         * @param shiftPattern Whether the pattern should be shifted by one pixel to the right
         */
        public Pattern (Object slot_1, Object slot_2, String pattern, boolean horizontal, boolean shiftPattern) {
            this(slot_1, slot_2, pattern, horizontal, shiftPattern, 0, 0, false);
        }
        /**@see #Pattern(Object, Object, String, boolean, boolean) **/
        public Pattern(Object slot_1, Object slot_2, String pattern, boolean horizontal) {
            this(slot_1, slot_2, pattern, horizontal, false);
        }

    }

    /**
     * Stores most used colors and helpful functions.
     * To get more control over pattern creation use.
     * {@code Bitmap.Pattern pattern = this.new Pattern()}
     * @see Color
     * @see Pattern
     */
    public class Colors {
        public final Color red() { return new Color(new int[]{255, 0, 0});}
        public final Color green() { return new Color(new int[]{0, 255, 0});}
        public final Color blue() { return new Color(new int[]{0, 0, 255});}

        public final Color yellow() { return new Color(new int[]{255, 255, 0});}
        public final Color purple() { return new Color(new int[]{255, 0, 255});}
        public final Color turquoise() { return new Color(new int[]{0, 255, 255});}
        public final Color orange() { return new Color(new int[]{255, 100, 0});}
        public final Color brown() { return new Color(new int[]{170, 80, 0});}
        public final Color pink() { return new Color(new int[]{255, 105, 180});}

        public final Color light_blue() { return mix(blue(), white()); }

        public final Color black() { return new Color(new int[]{0, 0, 0});}
        public final Color white() { return new Color(new int[]{255, 255, 255});}

        public final Color light_grey() { return new Color(new int[]{210, 210, 210});}
        public final Color grey() { return new Color(new int[]{140, 140, 140});}
        public final Color dark_grey() { return new Color(new int[]{70, 70, 70});}

        /**@return a transparent color*/
        public final Pattern transparent() {return new Pattern(white(), 1.0);}

        /**
         * @return a list of (almost) all colors available
         */
        public final Color[] list() {
            return new Color[]{
                    red(), green(), blue(),
                    yellow(), purple(), turquoise(), orange(), brown(), pink(),
                    black(), white(), grey()
            };
        }

        /** Returns a random color
         * @param true_random Whether the color should be selected completely random or from a predefined list.
         * @return the random color
         */
        public Color random (boolean true_random) {
            Random ran = new Random();
            Color color;
            if (true_random) {
                color = new Color(ran.nextInt(255), ran.nextInt(255), ran.nextInt(255));
            } else {
                Color[] list = list();
                color = list[ran.nextInt(list.length)];
            }
            return color; // Single exit point cause idk
        }


        /**
         * @return a random predefined color
         */
        public Color random() {
            return random(false);
        }


        /** Merges two color providers in a custom pattern.
         * @param slot_1 The first color provider
         * @param slot_2 The second color provider
         * @param custom_function A custom function
         * @return the created pattern
         * @throws IllegalArgumentException if the color providers are not of type Pattern or Color
         * @see Pattern#Pattern(Object, Object, Predicate)
         */
        public Pattern merge(Object slot_1, Object slot_2, Predicate<int[]> custom_function) {
            try {
                checkType(slot_1);
                checkType(slot_2);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("merged objects must either be of class Color or Pattern");
            }
            if (custom_function == null) {
                throw new IllegalArgumentException("custom_function needs to be set when using the \"custom\" fast pattern");
            } else {
                return new Pattern(slot_1, slot_2, custom_function);
            }


        }

        /** Merges the two color providers in a custom fast pattern
         * @param slot_1 The first color provider
         * @param slot_2 The second color provider
         * @param fast_pattern The fast pattern
         * @return the created pattern
         * @throws IllegalArgumentException
         * @see Pattern#evaluateFastPattern(String)
         */
        public Pattern merge(Object slot_1, Object slot_2, String fast_pattern) {
            try {
                checkType(slot_1);
                checkType(slot_2);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("merged objects must either be of class Color or Pattern");
            }
            return new Pattern(slot_1, slot_2, fast_pattern);
        }

        /** Mixes two colors with a specified balance
         * @param color_1 The first integer array with color information in rgb
         * @param color_2 The second integer array with color information in rgb
         * @param balance a double between 0.0 (color_1) and 1.0 (color_2) representing the balance
         * @return a new Color object
         * @throws IllegalArgumentException if the balance is out of bounds
         */
        public Color mix(int[] color_1, int[] color_2, double balance) {
            if (balance > 1.0 || balance < 0.0) {
                throw new IllegalArgumentException("balance should be a value between 0.0 (color_1) and 1.0 (color_2)");
            }

            int[] merged_color = new int[3];
            for (int i = 0; i<3; i++) {
                merged_color[i] = (int) (color_1[i] * (1.0 - balance) + color_2[i] * balance);
            }
            return new Color(merged_color);
        }

        /**
         * @param color_1 The first color
         * @param color_2 The second color
         * @param balance a double between 0.0 (color_1) and 1.0 (color_2) representing the balance
         * @return a new Color object
         * @throws IllegalArgumentException if the balance is out of bounds
         */
        public Color mix(Color color_1, Color color_2, double balance) {
            return mix(color_1.color, color_2.color, balance);
        }

        /** Mixes both colors with a 50:50 balance
         * @see #mix(Color, Color, double)
         */
        public Color mix(Color color_1, Color color_2) { return mix(color_1, color_2, 0.5); }

        /** Adds a transparency to the object
         * @see Pattern#Pattern(Object, double)
         */
        public Pattern opacity(Object slot_1, double opacity) { return new Pattern(slot_1, opacity); }
        /**Makes the current object semi-transparent*/
        public Pattern opacity(Object slot_1) { return opacity(slot_1, 0.5); }

        /** Converts the Color object into a Pattern
         * @param color the color
         * @return the created "normal" pattern
         */
        public Pattern patternFromColor(Color color) { return new Pattern(color, colors.black(), "normal");}
    }


    /**
     * Class to store and to create outline elements
     */
    public class Outline {
        boolean active; int thickness; Object color;

        /**
         * @param active Whether the outline is active or not
         * @param thickness The thickness of the outline in pixels
         * @param color The color of the outline (Pattern / Color)
         */
        public Outline (boolean active, int thickness, Object color) {
            this.active = active;
            this.thickness = thickness;
            checkType(color);
            this.color = color;
        }
        /**@see #Outline(boolean, int, Object) **/
        public Outline (boolean active) {
            this(active, 1, colors.white());
        }

        /**@see #Outline(boolean, int, Object) **/
        public Outline (int thickness, Object object) {
            this(true, thickness, object);
        }

        /**@see #Outline(boolean, int, Object) **/
        public Outline (int thickness) { this(true, thickness, colors.white());}

        /**@see #Outline(boolean, int, Object) **/
        public Outline () {this(true, 1, colors.white());}
    }

    /** Class that stores methods to create shapes
     * currently cross, circle and tree are available
     */
    public class Shapes {


        /** Method to create a cross at a specified position
         * @param pos_x The x-coordinate
         * @param pos_y The y-coordinate
         * @param size The size in pixel (diameter is size * 2)
         * @param thickness The thickness of the cross
         * @param color_provider The color p
         * @param outline
         */
        public void cross(int pos_x, int pos_y, int size, int thickness, Object color_provider, Outline outline){
            Pattern[] lockedPatterns = setAutoPattern(pos_x - size, pos_x + size, pos_y - size, pos_y + size, color_provider, true);
            if (outline != null) setAutoPattern(pos_x - size, pos_x + size, pos_y - size, pos_y + size, outline.color, true);

            checkType(color_provider);
            thickness = thickness / 2;
            size = size / 2;
            fillArea(pos_x - size, pos_y - thickness, pos_x + size, pos_y + thickness, color_provider, outline);
            fillArea(pos_x - thickness, pos_y - size, pos_x + thickness, pos_y + size, color_provider, outline);
            fillArea(pos_x - size, pos_y - thickness, pos_x + size, pos_y + thickness, color_provider);
            unlockAutoPattern(lockedPatterns);
        }

        // *** Overloads ***
        public void cross(int pos_x, int pos_y, int size, int thickness, Object color) {
            cross(pos_x, pos_y, size, thickness, color, new Outline(false));
        }
        public void cross(int pos_x, int pos_y, int size, int thickness, Outline outline) {
            cross(pos_x,pos_y, size, thickness, colors.black(), outline);
        }
        public void cross(int pos_x, int pos_y, int size, int thickness) {
            cross(pos_x, pos_y, size, thickness, colors.black(), new Outline(false));
        }


        /**
         * @param pos_x of the circle
         * @param pos_y of the circle
         * @param radius of the circle
         * @param color_provider a color provider for the body, can be either a Pattern or a Color
         * @param borderclip whether the circle should raise an exception if the accessed pixel is outside the canvas
         * @param outline_color_provider a color provider for the border object, set to null if unwanted
         * @throws IllegalArgumentException when borderclip is set to false and the accessed pixel is outside the canvas
         */
        public void circle(int pos_x, int pos_y, int radius, Object color_provider, boolean borderclip, Object outline_color_provider) {
            checkType(color_provider); if (outline_color_provider != null) checkType(outline_color_provider);

            //Gradient auto
            setAutoPattern(pos_x-radius, pos_x+radius, pos_y-radius, pos_y+radius, color_provider, false);
            if (outline_color_provider != null) setAutoPattern(pos_x-radius, pos_x+radius, pos_y-radius, pos_y+radius, outline_color_provider, false);

            for (int y = pos_y-radius; y < pos_y+radius; y++) {
                for (int x = pos_x-radius; x < pos_x+radius; x++) {
                    int equation = (int) (Math.pow(radius, 2) - (Math.pow(pos_x - x, 2) + Math.pow(pos_y-y, 2)));
                    double border_factor = radius * 2 - radius / 4.0;
                    if (outline_color_provider != null && ( equation + border_factor > 0 && equation + border_factor < border_factor || equation - border_factor < 0 && equation - border_factor > -border_factor ) ) {
                        try {
                            changePixel(x, y, outline_color_provider);
                        } catch (IllegalArgumentException e) {
                            if (borderclip) throw e;
                        }

                    }
                    else if (equation > 0) {
                        try {
                            changePixel(x, y, color_provider);
                        } catch (IllegalArgumentException e) {
                            if (borderclip) throw e;
                        }
                    }
                }
            }
        }

        // *** Overloads ***
        public void circle(int pos_x, int pos_y, int radius, Object color_provider, Object outline_color_provider) {circle(pos_x, pos_y, radius, color_provider, false, outline_color_provider);}
        public void circle(int pos_x, int pos_y, int radius, Object color_provider) {circle(pos_x, pos_y, radius, color_provider, null);}
        public void circle(int pos_x, int pos_y, int radius, Object color_provider, boolean borderclip) {circle(pos_x, pos_y, radius, color_provider, borderclip, null);}


        public void triangle(int pos_x, int pos_y, int size, Object color_provider, Object outline_color_provider) {
            System.out.println("NOT IMPLEMENTED");
            checkType(color_provider); if (outline_color_provider != null) checkType(outline_color_provider);
            setAutoPattern(pos_x-size, pos_x+size, pos_y-size, pos_y+size, color_provider, false);

            for (int y = pos_y-size; y < pos_y+size; y++) {
                for (int x = pos_x-size; x < pos_x+size; x++) {
                    break;
                }
            }
        }

        public void tree(int x_pos, int y_pos, double size) {
            lineV(y_pos, y_pos + (int) (22 * size), x_pos, colors.brown(), (int) (8 * size));
            shapes.circle(x_pos + ((int) (8 * size))/2, y_pos + (int) (22 * size), (int) (18 * size), colors.green().setAlpha(0.5), false);
            shapes.circle(x_pos + ((int) (8 * size))/2, y_pos + (int) (37 * size), (int) (14 * size), colors.green().setAlpha(0.6), false);
            shapes.circle(x_pos + ((int) (8 * size))/2, y_pos + (int) (50 * size), (int) (10 * size), colors.green().setAlpha(0.7), false);
        }
    }


    public static class Exceptions {
        // A RuntimeException (unchecked) does not need to be handled (must not be specified in method signature)
        // More about the differences: https://jaxenter.de/java-trickkiste-von-checked-und-unchecked-exceptions-1350

        /**
         * Is thrown when the accessed pixel is outside the canvas
         */
        public static class PixelOutOfBoundsException extends RuntimeException {
            public PixelOutOfBoundsException (String msg) {
                super(msg);
            }
        }
    }

    // *** Overloads ***

    /**@see #fillArea(int, int, int, int, Object, Outline)  */
    public void fillArea(int x_p1, int y_p1, int x_p2, int y_p2, Object color) {
        fillArea(x_p1, y_p1, x_p2, y_p2, color, new Outline(false));
    }

    public void fillWin() {
        fillWin(colors.black());
    }

    /**@see #lineH(int, int, int, Object, int) */
    public void lineH(int x_from, int x_to, int y_pos) {lineH(x_from, x_to, y_pos, colors.black(), 1);} 
    /**@see #lineH(int, int, int, Object, int) */
    public void lineH(int x_from, int x_to, int y_pos, int thickness) {lineH(x_from, x_to, y_pos, colors.black(), thickness);}
    /**@see #lineH(int, int, int, Object, int) */
    public void lineH(int y_pos, Object color_provider) {lineH(1, canvas_width, y_pos, color_provider, 1);}
    /**@see #lineH(int, int, int, Object, int) */
    public void lineH(int y_pos, Object color_provider, int thickness) {lineH(1, canvas_width, y_pos, color_provider, thickness);}
    /**@see #lineH(int, int, int, Object, int) */
    public void lineH(int y_pos) {lineH(y_pos, colors.black());}

    /**@see #lineV(int, int, int, Object, int) */
    public void lineV(int y_from, int y_to, int x_pos) {lineV(y_from, y_to, x_pos, colors.black(), 1);}
    /**@see #lineH(int, int, int, Object, int) */
    public void lineV(int y_from, int y_to, int x_pos, int thickness) {lineV(y_from, y_to, x_pos, colors.black(), thickness);}
    /**@see #lineH(int, int, int, Object, int) */
    public void lineV(int x_pos, Object color_provider) {lineV(1, canvas_height, x_pos, color_provider, 1);}
    /**@see #lineH(int, int, int, Object, int) */
    public void lineV(int x_pos, Object color_provider, int thickness) {lineV(1, canvas_height, x_pos, color_provider, thickness);}
    /**@see #lineH(int, int, int, Object, int) */
    public void lineV(int x_pos) {lineV(x_pos, colors.black());}


}


