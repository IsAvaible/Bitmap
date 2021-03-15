import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

public class Main {

    public static void main(String[] args) {
        //Overview.example();
        /*Chess chess = new Chess();
        Chess.Piece pawn = chess.get_piece(1, 1);
        Chess.Piece bishop = chess.get_piece(2, 0);
        chess.move(pawn, 1, 3);
        chess.move(bishop, 0, 2);
        chess.move(pawn, 1, 4);
        chess.move(bishop, 2, 4);
        chess.move(bishop, 2, 4);*/

        //test();
        // Other examples:
        // Overview.more_examples();
        // Animations
        // Animations.appear();
        // Animations.drive();
    }

    private static void test() {
        Bitmap bm = new Bitmap(800, 900);
        Bitmap.Colors col = bm.colors;
        Bitmap.PatternBuilders pbs = bm.patternBuilders;
        Bitmap.Pattern line_color = pbs.gradient(col.blue(), col.green()).build();
        Function<int[], Integer> function = arr -> (int) ((Math.pow((arr[0] - 400), 2) * 0.1 + 10) );
        bm.fillWin(pbs.smoothedFunction(line_color, pbs.gradient(col.white(), col.transparent()).build(), function).build());
        //bm.fillArea(400, 400, 800, 800, pbs.stripes(col.transparent(), pbs.gradient(col.blue(), col.green()).build()).withHorizontal().build());
        bm.render();
    }

}
