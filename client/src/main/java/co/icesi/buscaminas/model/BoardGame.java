package co.icesi.buscaminas.model;

import java.util.Random;

public class BoardGame {

    private Cell[][] board;
    private int mines;

    public int getMines() {
        return mines;
    }

    public int initGame(int n, int m, int mines) {
        this.mines = mines;
        board = new Cell[n][m];
        Random rd = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                board[i][j] = new Cell(false, 0);
            }
        }
        int placed = 0;
        int targetMines = Math.min(mines, n * m);
        while (placed < targetMines) {
            int k = rd.nextInt(n);
            int l = rd.nextInt(m);
            if (!board[k][l].isLandMine()) {
                board[k][l].setLandMine(true);
                placed++;
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                boolean isMine = board[i][j].isLandMine();
                if (!isMine) {
                    int minesAround = getMinesAround(i, j);
                    board[i][j].setValue(minesAround);
                }
            }
        }
        return placed;
    }

    public void showAll(boolean show) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                board[i][j].setShowAll(show);
            }
        }
    }

    private int getMinesAround(int i, int j) {
        int mines = 0;
        mines += i > 0 && board[i - 1][j].isLandMine() ? 1 : 0;
        mines += i < board.length - 1 && board[i + 1][j].isLandMine() ? 1 : 0;
        mines += j > 0 && board[i][j - 1].isLandMine() ? 1 : 0;
        mines += j < board[0].length - 1 && board[i][j + 1].isLandMine() ? 1 : 0;
        mines += i > 0 && j > 0 && board[i - 1][j - 1].isLandMine() ? 1 : 0;
        mines += i > 0 && j < board[0].length - 1 && board[i - 1][j + 1].isLandMine() ? 1 : 0;
        mines += i < board.length - 1 && j > 0 && board[i + 1][j - 1].isLandMine() ? 1 : 0;
        mines += i < board.length - 1 && j < board[0].length - 1 && board[i + 1][j + 1].isLandMine() ? 1 : 0;
        return mines;
    }

    public void printBoard() {
        System.out.println();
        System.out.print("   ");
        for (int i = 0; i < board[0].length; i++) {
            System.out.print(" " + i);
        }
        System.out.println();
        for (int i = 0; i < board.length; i++) {
            System.out.print(i + " [");
            for (int j = 0; j < board[0].length; j++) {
                System.out.print(" " + board[i][j]);
            }
            System.out.println(" ]");
        }
    }

    public boolean selectCell(int i, int j) {
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length) {
            throw new RuntimeException("Posición (" + i + ", " + j + ") no válida.");
        }
        Cell cell = board[i][j];
        if (!cell.isHide()) {
            return validWin();
        }
        if (cell.isMarked()) {
            cell.setMarked(false);
        }
        if (cell.isLandMine()) {
            cell.setHide(false);
            showAll(true);
            throw new RuntimeException("¡BOOM! Le pegaste a una mina en (" + i + ", " + j + "). ¡Juego terminado!");
        } else {
            showCells(i, j);
            return validWin();
        }
    }

    private boolean validWin() {
        boolean win = true;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                win &= !board[i][j].isHide() || board[i][j].isLandMine();
            }
        }
        return win;
    }

    private void showCells(int i, int j) {
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length) {
            return;
        }
        Cell cell = board[i][j];
        if (!cell.isHide() || cell.isLandMine()) {
            return;
        }
        cell.setHide(false);
        cell.setMarked(false);

        if (cell.getValue() == 0) {
            for (int di = -1; di <= 1; di++) {
                for (int dj = -1; dj <= 1; dj++) {
                    if (di != 0 || dj != 0) {
                        showCells(i + di, j + dj);
                    }
                }
            }
        }
    }

    public Cell[][] getBoard() {
        return board;
    }

    public void markCell(int i, int j) {
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length) {
            throw new RuntimeException("Posición (" + i + ", " + j + ") no válida.");
        }
        Cell cell = board[i][j];
        if (!cell.isHide()) {
            throw new RuntimeException("La celda (" + i + ", " + j + ") ya está descubierta.");
        }
        cell.setMarked(!cell.isMarked());
    }
}
