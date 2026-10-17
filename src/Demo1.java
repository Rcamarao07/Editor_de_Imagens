import pt.iscte.greyditor.Greyditor;
import pt.iscte.greyditor.Editor;
import pt.iscte.greyditor.Selection;
void main() {
    Greyditor configuration = new Greyditor("Demo");
    configuration.addFilter("Invert", this::invert);
    configuration.addFilter("Darken", this::darken, 0, 255);
    configuration.addEffect("Grid", this::grid);
    configuration.addEffect("Lines", this::lines, 0, 50);
    configuration.addOperation("Square", this::square);
    configuration.addLoadOperation("Load");
    configuration.addSaveOperation("Save");
    configuration.open("monalisa.jpg");
}
int invert(int tone) {
    return 255 - tone;
}
int darken(int tone, int intensity) {
    return Math.max(0, tone - intensity);
}
void grid(int[][] image) {
    int hspace = (image[0].length + 2) / 3;
    int vspace = (image.length + 2) / 3;

    for (int y = vspace; y < image.length; y += vspace)
        for (int x = 0; x < image[y].length; x++)
            image[y][x] = 200;

    for (int x = hspace; x < image[0].length; x += hspace)
        for (int y = 0; y < image.length; y++)
            image[y][x] = 200;
}
void lines(int[][] image, int spacing) {
    if (spacing == 0)
        return;
    for (int y = 0; y < image.length; y += spacing)
        for (int x = 0; x < image[y].length; x++)
            image[y][x] = 0;
}
int[][] square(int[][] image) {
    int side = Math.min(image.length, image[0].length);
    int[][] square = new int[side][side];
    for (int y = 0; y < side; y++)
        for (int x = 0; x < side; x++)
            square[y][x] = image[y][x];

    return square;
}
int[][] darkenArea(int[][] image, Editor editor) {
    Selection selection = editor.getSelection();
    if (selection == null) {
        editor.message("Please select an area of the image.");
    }
    else {
        int factor = editor.getInteger("Intensity?");
        for (int y = selection.y(); y < selection.y() + selection.height(); y++)
            for (int x = selection.x(); x < selection.x() + selection.width(); x++)
                image[y][x] = Math.max(0, image[y][x] - factor);
    }
    return null;
}