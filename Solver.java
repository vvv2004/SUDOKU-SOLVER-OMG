import java.util.ArrayList;
import java.util.Arrays;

public class Solver {
    private char[] ALLOWED_CHARACTERS = {49, 50, 51, 52, 53, 54, 55, 56, 57};
    private char[][] board;

    public Solver(String input) {
        System.out.println(App.printMatrix(convertInputToMatrix(input)));
        board = convertInputToMatrix(input);
    }

    public String solveSudoku() throws SudokuException {
        char[][] possibleSolutions = new char[81][9];

        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                int currentCellId = (9 * y) + x;
                char currentSymbol = board[y][x];

                if (currentSymbol == '-') {
                    possibleSolutions[currentCellId] = checkPossibleSolutionForCell(currentCellId);
                } else if (currentSymbol < 49 || currentSymbol > 57) {
                    throw new SudokuException("You placed invalid symbol");
                }
            }
        }

        int solvedCells = 0;
        biggerLoop: while (solvedCells <= possibleSolutions.length) {
            int cellId = 0;
            while (countEmptyElements(possibleSolutions[cellId]) != 8) {
                cellId++;
                if(cellId > 80){
                    break biggerLoop;
                }
            }
            char temp = possibleSolutions[cellId][0];
            possibleSolutions[cellId][0] = 3;
            possibleSolutions[cellId][1] = temp;
            for(int currentCellId : getIdOfOptionsThatShouldBeRemovedHorizontal(cellId, possibleSolutions)){
                removeElementFromArray(temp, possibleSolutions[currentCellId]);
                sort(possibleSolutions[currentCellId]);
            }
            for(int currentCellId : getIdOfOptionsThatShouldBeRemovedVertical(cellId, possibleSolutions)){
                removeElementFromArray(temp, possibleSolutions[currentCellId]);
                sort(possibleSolutions[currentCellId]);
            }
            for(int currentCellId : getIfOfOptionsThatShouldBeRemovedBox(cellId, possibleSolutions)){
                removeElementFromArray(temp, possibleSolutions[currentCellId]);
                sort(possibleSolutions[currentCellId]);
            }

            solvedCells++;
        }

        mergeSolutionsWithBoard(possibleSolutions);

        return App.printMatrix(board);
    }

    private void mergeSolutionsWithBoard(char[][] solutions) {
        int x;
        int y;

        for (int i = 0; i < solutions.length; i++) {
           if(solutions[i][0] == 3){
               x = i % 9;
               y = i / 9;

               board[y][x] = solutions[i][1];
           }
        }
    }

    //=======================================SOLVING METHODS================================================================
    private ArrayList<Integer> getIdOfOptionsThatShouldBeRemovedHorizontal(int cellId, char[][] solutions) {
        ArrayList<Integer> output = new ArrayList<>();
        int x = cellId % 9;
        int currentCell = cellId - x;
        int cx = 0;

        while (cx <= 8) {
            if (solutions[currentCell][0] != 0 || solutions[currentCell][0] != 3) {
                if (currentCell != cellId) {
                    if (isSymbolInArray(solutions[cellId][1], solutions[currentCell])) {
                        output.add(currentCell);
                    }
                }
            }

            cx++;
            currentCell++;
        }

        return output;
    }

    private ArrayList<Integer> getIdOfOptionsThatShouldBeRemovedVertical(int cellId, char[][] solutions) {
        ArrayList<Integer> output = new ArrayList<>();
        int y = cellId / 9;
        int currentCell = cellId - 9 * y;
        int cy = 0;

        while (cy <= 8) {
            if (solutions[currentCell][0] != 0 || solutions[currentCell][0] != 3) {
                if (currentCell != cellId) {
                    if (isSymbolInArray(solutions[cellId][1], solutions[currentCell])) {
                        output.add(currentCell);
                    }
                }
            }

            cy++;
            currentCell += 9;
        }

        return output;
    }

    private ArrayList<Integer> getIfOfOptionsThatShouldBeRemovedBox(int cellId, char[][] solutions){
        ArrayList<Integer> output = new ArrayList<>();
        int boxStartCellId = getBoxStartCellId(cellId);
        int boxEndCellId = (boxStartCellId + 9 * 2) + 2;
        int currentCell = boxStartCellId;
        int counter = 0;

        while(currentCell <= boxEndCellId){
            if (solutions[currentCell][0] != 0 || solutions[currentCell][0] != 3) {
                if (currentCell != cellId) {
                    if (isSymbolInArray(solutions[cellId][1], solutions[currentCell])) {
                        output.add(currentCell);
                    }
                }
            }

            counter++;
            if(counter < 3){
                currentCell++;
            }else{
                counter = 0;
                currentCell += 7;
            }
        }

        return output;
    }

    //=======================================GETTING ALL POSSIBLE SOLUTION NUMBERS==========================================
    private char[] checkPossibleSolutionForCell ( int cellId){
            int x = cellId % 9;
            int y = cellId / 9;
            char[] output = new char[9];

            //merging 3 arrays
            char[] usedNumbers = mergeArrays(mergeArrays(checkTakenNumbersInRow(y), checkTakenNumbersInCol(x)),
                    checkTakenNumbersInBox(getBoxStartCellId(cellId)));

            int j = 0;
            for (int i = 49; i < 58; i++) {
                char currentSymbol = (char) i;

                if (!isSymbolInArray(currentSymbol, usedNumbers)) {
                    output[j] = currentSymbol;
                    j++;
                }
            }

            return output;
        }

    private char[] checkTakenNumbersInRow ( int y){
            char[] output = new char[9];

            int i = 0;
            for (char c : board[y]) {
                if (c != '-') {
                    output[i] = c;
                    i++;
                }
            }

            return output;
        }

    private char[] checkTakenNumbersInCol ( int x){
            char[] output = new char[9];
            int i = 0;

            for (int y = 0; y < board[x].length; y++) {
                if (board[y][x] != '-') {
                    output[i] = board[y][x];
                    i++;
                }
            }

            return output;
        }

    private char[] checkTakenNumbersInBox ( int startingCellCoordinates){
            char[] output = new char[9];
            int boxX = startingCellCoordinates % 9;
            int boxY = startingCellCoordinates / 9;

            int i = 0;
            int colCounter = 0;
            int rowCounter = 0;

            //loop to cycle through a box
            while (rowCounter <= 2) {
                if (board[boxY][boxX] != '-') {
                    output[i] = board[boxY][boxX];
                    i++;
                }

                if (colCounter >= 2) {
                    colCounter = 0;
                    boxX -= 2;
                    boxY++;
                    rowCounter++;
                } else {
                    colCounter++;
                    boxX++;
                }

            }

            return output;
        }

    private int getBoxStartCellId ( int cellId){
            int x = cellId % 9;
            int y = cellId / 9;
            int boxX = -1;
            int boxY = -1;

            if (x >= 0 && x < 3) {
                boxX = 0;
            } else if (x >= 3 && x < 6) {
                boxX = 3;
            } else if (x >= 6) {
                boxX = 6;
            }

            if (y >= 0 && y < 3) {
                boxY = 0;
            } else if (y >= 3 && y < 6) {
                boxY = 3;
            } else if (y >= 6) {
                boxY = 6;
            }

            return (boxY * 9) + boxX;
        }

        //===============================================ARRAY MANIPULATION=====================================================
        private boolean isSymbolInArray ( char symbol, char[] array){
            for (char ch : array) {
                if (symbol == ch) {
                    return true;
                }
            }

            return false;
        }

        private char[] mergeArrays ( char[] array1, char[] array2){
            for (char c : array1) {
                if (!isSymbolInArray(c, array2)) {
                    int j = 0;
                    while (array2[j] != 0) {
                        j++;
                    }

                    array2[j] = c;
                }
            }

            return array2;
        }

        private int countEmptyElements ( char[] array){
            int counter = 0;

            for (char c : array) {
                if (c == 0) {
                    counter++;
                }
            }

            return counter;
        }

        private char[][] convertInputToMatrix (String input){
            char[] inputAsCharArray = input.toCharArray();
            char[][] output = new char[9][9];

            int tracker = 0;
            for (int i = 0; i < output.length; i++) {
                for (int j = 0; j < output[i].length; j++) {
                    output[i][j] = inputAsCharArray[tracker];
                    tracker++;
                }
            }

            return output;
        }

    public void removeElementFromArray(char element, char[] array){
        for (int i = 0; i < array.length; i++) {
            if(array[i] == element){
                array[i] = 0;
            }
        }
    }

    public void sort(char[] array){
        for (int i = 0; i < array.length - 1; i++)
            for (int j = 0; j < array.length - i - 1; j++)
                if (array[j] < array[j + 1]) {
                    // swap arr[j+1] and arr[j]
                    char temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
    }
}