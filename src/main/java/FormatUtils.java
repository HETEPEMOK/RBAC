import java.util.List;

public class FormatUtils {
    public static String formatTable(String[] headers, List<String[]> rows)
    {
        if (headers == null || headers.length == 0) return "";
        int[] columnWidths = new int[headers.length];

        for (int i = 0; i < headers.length; i++)
        {
            columnWidths[i] = headers[i].length();
        }

        for (String[] row: rows)
        {
            for (int i = 0; i < Math.min(row.length, columnWidths.length); i++)
            {
                if (row[i] != null)
                {
                    columnWidths[i] = Math.max(columnWidths[i], row[i].length());
                }
            }
        }

        StringBuilder table = new StringBuilder();

        table.append("|");
        for (int i = 0; i < headers.length; i++)
        {
            table.append(" ").append(padRight(headers[i], columnWidths[i])).append("|");
        }
        table.append("\n");

        table.append("+");
        for (int width: columnWidths)
        {
            table.append("-".repeat(width + 2)).append("+");
        }
        table.append("\n");

        for (String[] row: rows)
        {
            table.append("|");
            for (int i = 0; i < columnWidths.length; i++)
            {
                String cell = i < row.length ? row[i] : "";
                table.append(" ").append(padRight(String.format("%s %s", cell, columnWidths[i]), columnWidths[i])).append(" |");
            }
            table.append("\n");
        }

        table.append("+");
        for (int width: columnWidths)
        {
            table.append("-".repeat(width + 2)).append("+");
        }
        table.append("\n");

        return table.toString();
    }

    public static String formatBox(String text)
    {
        String[] lines = text.split("\n");
        int maxLength = 0;
        for (String line: lines)
        {
            maxLength = Math.max(maxLength, line.length());
        }

        StringBuilder box = new StringBuilder();
        box.append("+").append("-".repeat(maxLength + 2)).append("+\n");
        for (String line: lines)
        {
            box.append("| ").append(padRight(line, maxLength)).append(" |\n");
        }
        box.append("+").append("-".repeat(maxLength + 2)).append("+\n");
        return box.toString();
    }

    public static String formatHeader(String text)
    {
        return "\n=== " + text + " " + "=".repeat(Math.max(0,60-text.length() - 5)) + "\n";
    }

    public static String truncate(String text, int maxLength)
    {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padLeft(String text, int length)
    {
        if (text == null) text = "";
        return String.format("%" + length + "s", text);
    }
    public static String repeat(String text, int count)
    {
        return text.repeat(Math.max(0, count));
    }
    public static String padRight(String text, int length)
    {
        if (text == null) text = "";
        return String.format("%-" + length + "s", text);
    }
    public static String center(String text, int length)
    {
        if (text == null)
        {
            if (length > 0)
            {
                return " ".repeat(length);
            }
            return "";
        }
        int padding = Math.max(0,length - text.length());
        int leftPad = padding / 2;
        int rightPad = padding - leftPad;
        return " ".repeat(leftPad) + text + " ".repeat(rightPad);
    }
}
