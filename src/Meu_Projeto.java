import pt.iscte.greyditor.Editor;
import pt.iscte.greyditor.Greyditor;
import pt.iscte.greyditor.Selection;

void main() {
    Greyditor configuration = new Greyditor("Demo");
    configuration.addFilter("Invert", this::invert, 0, 100);
    configuration.addFilter("Contraste", this::contraste, 0, 100);
    configuration.addFilter("Darken/Ligthen", this::darkenLighten, -255, 255);
    configuration.addFilter("Grain", this::grain, 0, 100);
    configuration.addEffect("Horizontal mirror", this::horizontalmirror);
    configuration.addEffect("Vertical mirror", this::verticalmirror);
    configuration.addEffect("Grid", this::grid);
    configuration.addEffect("Margem", this::margin);
    configuration.addEffect("Vignette", this::vignette, -100, 100);
    configuration.addEffect("Lines", this::lines, 0, 50);
    configuration.addOperation("Crop", this::crop);
    configuration.addOperation("Expand", this::expand);
    configuration.addOperation("Postorize", this::posterize);
    configuration.addOperation("Rotate", this::rotate);
    configuration.addOperation("Blur", this::blur);
    configuration.addOperation("Antique", this::antique);
    configuration.addOperation("Retro", this::retro);
    CopyClass operations = new CopyClass();
    configuration.addOperation("Cut", operations::cut);
    configuration.addOperation("Copy", operations::copy);
    configuration.addOperation("Paste", operations::paste);
    configuration.addOperation("Undo", undo::undo);
    configuration.addOperation("Square", this::square);
    configuration.addLoadOperation("Load");
    configuration.addSaveOperation("Save");
    configuration.open("monalisa.jpg");
}

//Fase 1//

int darkenLighten(int tone, int intensity) {
    return Math.min(255,Math.max(0, tone + intensity));
}

int contraste(int tone, int intensity) {
    int contraste = 0;
    if (tone >= 128)
        contraste = tone + intensity;
    else
        contraste = tone - intensity;
    if (contraste >= 255)
        return 255;
    if (contraste <= 0)
        return 0;
    return contraste;
}

void horizontalmirror(int[][] image) {
    int width = image[0].length;
    for (int i = 0; i < image.length; i++)
        for (int j = 0; j < width / 2; j++) {
            int guardar = image[i][j];
            image[i][j] = image[i][width - j - 1];
            image[i][width - j - 1] = guardar;
        }
}

void verticalmirror(int[][] image) {
    int height = image.length;
    for (int i = 0; i < height / 2; i++)
        for (int j = 0; j < image[0].length; j++) {
            int guardar = image[i][j];
            image[i][j] = image[height - i - 1][j];
            image[height - i - 1][j] = guardar;
        }
}

int grain(int tone, int intensity)  {
    int intensidade = (int) (Math.random() * (intensity + 1));
    int fim = 0;
    if (Math.random() > 0.5)
        fim = tone + intensidade;
    else {
        fim = tone - intensidade;
    }
    if (fim > 255)
        return 255;
    if (fim < 0)
        return 0;
    return fim;
}

void margin(int[][] image) {
    for (int i = 0; i < image.length / 15; i++)
        for (int j = 0; j < image[0].length; j++)
            image[i][j] = 255;
    for (int i = 0; i < image.length; i++)
        for (int j = 0; j < image[0].length / 15; j++)
            image[i][j] = 255;
    for (int i = image.length - image.length / 15; i < image.length; i++)
        for (int j = 0; j < image[0].length; j++)
            image[i][j] = 255;
    for (int i = 0; i < image.length; i++)
        for (int j = image[0].length - image[0].length / 15; j < image[0].length; j++)
            image[i][j] = 255;
}

void vignette(int[][] image, int intensity) {
    int centerY = image.length / 2;
    int centerX = image[0].length / 2;
    double radius = (Math.sqrt(Math.pow(centerY, 2) + Math.pow(centerX, 2)));
    for (int i = 0; i < image.length; i++)
        for (int j = 0; j < image[0].length; j++) {
            double distance = Math.sqrt((Math.pow(i - centerY, 2)) + Math.pow(j - centerX, 2));
            double proximity = distance / radius;
            int dark = (int) (proximity * intensity);
            int darken = image[i][j] - dark;
            if (darken < 0)
                darken = 0;
            if (darken > 255)
                darken = 255;
            image[i][j] = darken;
        }
}

//Fase 2//

int[][] crop(int[][] image, Editor editor) {
    Selection selection = editor.getSelection();
    if (selection == null) {
        editor.message("Please select an area of the image.");
        return null;
    }
    undo.saveImage(image);
    int[][] croped = new int[selection.height()][selection.width()];
    for (int i = 0; i < selection.height(); i++)
        for (int j = 0; j < selection.width(); j++)
            croped[i][j] = image[selection.y() + i][selection.x() + j];
    return croped;
}

int[][] expand(int[][] image, Editor editor) {
    undo.saveImage(image);
    int heigth = editor.getInteger("Enter the desired height to be added:");
    int width = editor.getInteger("Enter the desired width to be added:");
    int[][] expanded = new int[image.length + heigth][image[0].length + width];
    for (int i = 0; i < expanded.length; i++)
        for (int j = 0; j < expanded[0].length; j++)
            expanded[i][j] = 0;
    for (int i = 0; i < image.length; i++)
        for (int j = 0; j < image[0].length; j++)
            expanded[i][j] = image[i][j];
    return expanded;
}

int[][] posterize(int[][] image, Editor editor) {
    undo.saveImage(image);
    int colors = editor.getInteger("Enter the desired number of colors:");
    int[][] postorized = new int[image.length][image[0].length];
    int step;
    for (int i = 0; i < image.length; i++)
        for (int j = 0; j < image[0].length; j++) {
            step = (image[i][j] * colors) / 256; //ver o degrau em que a cor está
            postorized[i][j] = (step * 255) / colors; //mudar a cor para a cor do degrau
        }
    return postorized;
}

int[][] rotate(int[][] image, Editor editor) {
    undo.saveImage(image);
    int[][] rotate = new int[image[0].length][image.length]; //criar uma imagem nova que tem o tamanho da original mas transposta
    for (int i = 0; i < image.length; i++)
        for (int j = 0; j < image[0].length; j++)
            rotate[j][image.length - i - 1] = image[i][j]; //meter as linhas nas colunas e meter as colunas nas linhas a começar pelo fim
    return rotate;
}

int[][] blur(int[][] image, Editor editor) {
    undo.saveImage(image);
    int[][] blur = new int[image.length][image[0].length];
    int r = editor.getInteger("What is the radius of the blur?");
    for (int i = 0; i < blur.length; i++) {
        for (int j = 0; j < blur[0].length; j++) {
            int s = 0;
            int c = 0;
            for (int y = -r; y <= r; y++)
                for (int x = -r; x <= r; x++) {//para verificar os dois lados, tenho de começar no negativo para ir detrás até há frente do sitio onde quero
                    int neighborY = i + y; //verificar colunas(pixeis em cima e embaixo do ponto que estou)
                    int neighborX = j + x; //verificar linhas(pixeis esquerda e direita do ponto que estou)
                    if (neighborY >= 0 && neighborY < image.length && neighborX >= 0 && neighborX < image[0].length) {
                        s += image[neighborY][neighborX]; //somar todas as cores dos pixeis
                        c++; //contar os pixeis
                    }
                }
            blur[i][j] = s / c;
        }
    }
    return blur;
}

int[][] antique(int[][] image, Editor editor) {
    undo.saveImage(image);
    int intensity = 25;
    for (int i = 0; i < image.length; i++)
        for (int j = 0; j < image[0].length; j++)
            image[i][j] = grain(image[i][j], intensity);
    vignette(image, intensity);
    margin(image);
    return image;
}

int[][] retro(int[][] image, Editor editor) {
    undo.saveImage(image);
    image = blur(image, editor);
    for (int i = 0; i < image.length; i++)
        for (int j = 0; j < image[0].length; j++)
            image[i][j] = contraste(image[i][j], 25);
    vignette(image, 50);
    return image;
}

//Fase 3//

public class CopyClass {
    int[][] copy = null;
    int[][] cut = null;

    int[][] copy(int[][] image, Editor editor) {

        Selection selection = editor.getSelection();
        if (selection == null) {
            editor.message("Please select an area of the image.");
            return null;
        }
        int[][] copy1 = new int[selection.height()][selection.width()];
        for (int i = 0; i < selection.height(); i++)
            for (int j = 0; j < selection.width(); j++)
                copy1[i][j] = image[selection.y() + i][selection.x() + j];
        copy = copy1;
        editor.message("Copied to the clipboard.");
        return image;
    }

    int[][] cut(int[][] image, Editor editor) {
        Selection selection = editor.getSelection();
        if (selection == null)
            editor.message("Please select an area of the image.");
        else {
            undo.saveImage(image);
            int[][] cut1 = new int[selection.height()][selection.width()];
            for (int i = 0; i < selection.height(); i++)
                for (int j = 0; j < selection.width(); j++) {
                    cut1[i][j] = image[i + selection.y()][j + selection.x()];
                    image[i + selection.y()][j + selection.x()] = 255;
                }
            cut = cut1;
        }
        return null;
    }

    int[][] paste(int[][] image, Editor editor) {
        Selection selection = editor.getSelection();
        if (selection == null) {
            editor.message("Please select an area of the image.");
            return null;
        }
        undo.saveImage(image);
        int[][] copy1 = copy;
        int[][] cut1 = cut;
        if (copy1 != null) {
            int heigthCopia = Math.min(selection.height(), copy1.length);
            int widthCopia = Math.min(selection.width(), copy1[0].length);
            for (int i = 0; i < heigthCopia; i++)
                for (int j = 0; j < widthCopia; j++)
                    image[i + selection.y()][j + selection.x()] = copy1[i][j];
        }
        if (copy1 == null && cut1 == null)
            editor.message("Please copy an area of the image.");
        if (cut1 != null) {
            int heigthCut = Math.min(selection.height(), cut1.length);
            int widthCut = Math.min(selection.width(), cut1[0].length);
            for (int i = 0; i < heigthCut; i++)
                for (int j = 0; j < widthCut; j++)
                    image[i + selection.y()][j + selection.x()] = cut1[i][j];
            cut = null;
        }
        return image;
    }
}

public class Undo {
    private int index = 0;
    int[][][] save = new int[10][][];

    void saveImage(int[][] image) {
        if (index < save.length) {
            save[index] = new int[image.length][image[0].length];
            for (int i = 0; i < image.length; i++) {
                for (int j = 0; j < image[0].length; j++) {
                    save[index][i][j] = image[i][j];
                }
            }
            index++;
        } else {
            for (int i = 0; i < save.length - 1; i++) {
                save[i] = save[i + 1];
                save[save.length - 1] = new int[image.length][image[0].length];
            }
            for (int i = 0; i < image.length; i++) {
                for (int j = 0; j < image[0].length; j++)
                    save[save.length - 1][i][j] = image[i][j];
                index = save.length;
            }
        }
    }

    int[][] undo(int[][] image) {
        if (index > 0) {
            int[][] saved = save[index - 1];
            index--;
            return saved;
        } else {
            return image;
        }
    }
}

Undo undo = new Undo();

//GitHub//

int invert(int tone, int intensity) {
    int invertido = 255 - tone;
    int diferença = invertido - tone;
    int result = tone + (diferença * intensity) / 100;
    return result;
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
    undo.saveImage(image);
    int side = Math.min(image.length, image[0].length);
    int[][] square = new int[side][side];
    for (int y = 0; y < side; y++)
        for (int x = 0; x < side; x++)
            square[y][x] = image[y][x];

    return square;
}

int[][] darkenArea(int[][] image, Editor editor) {
    undo.saveImage(image);
    Selection selection = editor.getSelection();
    if (selection == null) {
        editor.message("Please select an area of the image.");
    } else {
        int factor = editor.getInteger("Intensity?");
        for (int y = selection.y(); y < selection.y() + selection.height(); y++)
            for (int x = selection.x(); x < selection.x() + selection.width(); x++)
                image[y][x] = Math.max(0, image[y][x] - factor);
    }
    return null;
}

