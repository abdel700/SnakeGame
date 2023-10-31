import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.Stack;
import java.awt.Point;

public class NavigationAgent extends Agent {
    private JFrame frame;
    private JPanel agentPanel;
    private int gridSize = 10; // Adjust the grid size as needed
    private boolean[][] grid;
    private Point start = new Point(0, 0); // Fixed start point (green square)
    private Point end = new Point(9, 9);   // Fixed end point (blue square)
    private int obstacleCount = 20; // Adjust the number of obstacles as needed
    private Stack<Point> path;
    private boolean navigating = false;

    private int agentX = 0; // Agent's X position on the grid
    private int agentY = 0; // Agent's Y position on the grid

    protected void setup() {
        SwingUtilities.invokeLater(this::createGUI);
    }

    public NavigationAgent() {
        grid = new boolean[gridSize][gridSize];
        generateObstacles();
        path = findPath(start, end);
    }

    private void generateObstacles() {
        Random random = new Random();
        for (int i = 0; i < obstacleCount; i++) {
            int x, y;
            do {
                x = random.nextInt(gridSize);
                y = random.nextInt(gridSize);
            } while (x == start.x && y == start.y || x == end.x && y == end.y);

            grid[x][y] = true;
        }
    }

    private void createGUI() {
        frame = new JFrame("Jade Navigation Agent");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        agentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int cellWidth = getWidth() / gridSize;
                int cellHeight = getHeight() / gridSize;

                for (int x = 0; x < gridSize; x++) {
                    for (int y = 0; y < gridSize; y++) {
                        if (grid[x][y]) {
                            g.setColor(Color.BLACK);
                            g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                        } else if (start.equals(new Point(x, y))) {
                            g.setColor(Color.GREEN);
                            g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                        } else if (end.equals(new Point(x, y))) {
                            g.setColor(Color.BLUE);
                            g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                        } else {
                            g.setColor(Color.LIGHT_GRAY);
                            g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                        }
                        g.setColor(Color.BLACK);
                        g.drawRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                    }
                }

                if (navigating && !path.isEmpty()) {
                    Point nextPosition = path.peek();
                    g.setColor(Color.RED);
                    g.fillRect(nextPosition.x * cellWidth, nextPosition.y * cellHeight, cellWidth, cellHeight);
                }
            }
        };
        agentPanel.setPreferredSize(new Dimension(400, 400));
        frame.add(agentPanel, BorderLayout.CENTER);

        // Add the "Move" button to the GUI
        JButton moveButton = new JButton("Move");
        moveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                moveAgentManually();
            }
        });
        frame.add(moveButton, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);

        // Start the agent's automatic navigation behavior
        addBehaviour(new AutoMoveBehavior());
    }

    private void moveAgentManually() {
        if (!path.isEmpty()) {
            Point nextPosition = path.pop();
            int dx = nextPosition.x - agentX;
            int dy = nextPosition.y - agentY;
            agentX = nextPosition.x;
            agentY = nextPosition.y;
            agentPanel.repaint();

            if (dx > 0) {
                moveRight();
            } else if (dx < 0) {
                moveLeft();
            } else if (dy > 0) {
                moveDown();
            } else if (dy < 0) {
                moveUp();
            }

            navigating = true;
        }
    }

    private class AutoMoveBehavior extends CyclicBehaviour {
        public void action() {
            if (!navigating && !path.isEmpty()) {
                moveAgentAutomatically();
            }
        }
    }

    private void moveAgentAutomatically() {
        Point nextPosition = path.pop();
        int dx = nextPosition.x - agentX;
        int dy = nextPosition.y - agentY;
        agentX = nextPosition.x;
        agentY = nextPosition.y;
        agentPanel.repaint();

        if (dx > 0) {
            moveRight();
        } else if (dx < 0) {
            moveLeft();
        } else if (dy > 0) {
            moveDown();
        } else if (dy < 0) {
            moveUp();
        }

        navigating = true;
    }

    private void moveUp() {
        if (agentY > 0 && !grid[agentX][agentY - 1]) {
            navigating = true;
            agentY--;
        }
    }

    private void moveDown() {
        if (agentY < gridSize - 1 && !grid[agentX][agentY + 1]) {
            navigating = true;
            agentY++;
        }
    }

    private void moveLeft() {
        if (agentX > 0 && !grid[agentX - 1][agentY]) {
            navigating = true;
            agentX--;
        }
    }

    private void moveRight() {
        if (agentX < gridSize - 1 && !grid[agentX + 1][agentY]) {
            navigating = true;
            agentX++;
        }
    }

    private Stack<Point> findPath(Point start, Point end) {
        Stack<Point> path = new Stack<>();
        Stack<Point> visited = new Stack<>();
        Stack<Point> toExplore = new Stack<>();
        toExplore.push(start);

        while (!toExplore.isEmpty()) {
            Point current = toExplore.pop();
            if (current.equals(end)) {
                // Reconstruct the path
                while (!current.equals(start)) {
                    path.push(current);
                    current = visited.pop();
                }
                path.push(start);
                break;
            }

            visited.push(current);

            int x = current.x;
            int y = current.y;

            // Explore neighbors
            if (x > 0 && !visited.contains(new Point(x - 1, y)) && !toExplore.contains(new Point(x - 1, y)) && !grid[x - 1][y]) {
                toExplore.push(new Point(x - 1, y));
            }
            if (x < gridSize - 1 && !visited.contains(new Point(x + 1, y)) && !toExplore.contains(new Point(x + 1, y)) && !grid[x + 1][y]) {
                toExplore.push(new Point(x + 1, y));
            }
            if (y > 0 && !visited.contains(new Point(x, y - 1)) && !toExplore.contains(new Point(x, y - 1)) && !grid[x][y - 1]) {
                toExplore.push(new Point(x, y - 1));
            }
            if (y < gridSize - 1 && !visited.contains(new Point(x, y + 1)) && !toExplore.contains(new Point(x, y + 1)) && !grid[x][y + 1]) {
                toExplore.push(new Point(x, y + 1));
            }
        }

        return path;
    }
}
