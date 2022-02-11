package cn.minecon.areaprotect.dynmap;

public class MessageColor {
	public static String htmlColors(String input) {
        input = input.replace("<BLACK>", "</span><span style=\"color:black\">");
        input = input.replace("<DARK_BLUE>", "</span><span style=\"color:#00002A\">");
        input = input.replace("<DARK_GREEN>", "</span><span style=\"color:#002A00\">");
        input = input.replace("<DARK_AQUA>", "</span><span style=\"color:#002A2A\">");
        input = input.replace("<DARK_RED>", "</span><span style=\"color:#2A0000\">");
        input = input.replace("<DARK_PURPLE>", "</span><span style=\"color:#2A002A\">");
        input = input.replace("<GOLD>", "</span><span style=\"color:#2A2A00\">");
        input = input.replace("<GRAY>", "</span><span style=\"color:#2A2A2A\">");
        input = input.replace("<DARK_GRAY>", "</span><span style=\"color:#151515\">");
        input = input.replace("<BLUE>", "</span><span style=\"color:#15153F\">");
        input = input.replace("<GREEN>", "</span><span style=\"color:#153F15\">");
        input = input.replace("<AQUA>", "</span><span style=\"color:#153F3F\">");
        input = input.replace("<RED>", "</span><span style=\"color:#3F1515\">");
        input = input.replace("<LIGHT_PURPLE>", "</span><span style=\"color:#3F153F\">");
        input = input.replace("<YELLOW>", "</span><span style=\"color:#3F3F15\">");
        input = input.replace("<WHITE>", "</span><span style=\"color:#3F3F3F\">");
        input = input.replace("<BOLD>", "</span><span style=\"font-weight:bold\">");
        input = input.replace("<UNDERLINE>", "</span><span style=\"text-decoration:underline\">");
        input = input.replace("<ITALIC>", "</span><span style=\"font-style:italic\">");
        input = input.replace("<STRIKE>", "</span><span style=\"text-decoration:line-through\">");
        input = input.replace("<MAGIC>", "");
        input = input.replace("<RESET>", "");
        return "<span>" + input + "</span>";
    }
}
