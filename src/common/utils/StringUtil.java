/*
 * String Util
 */
package common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iychoi
 */
public class StringUtil {
    public static String[] splitWithNonAlphabet(String str) {
        List<String> arr = new ArrayList<String>();
        
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<str.length();i++) {
            boolean foundDelimeter = false;
            char ch = str.charAt(i);
            if((ch >= 'A' && ch <= 'Z')
                    || (ch >= 'a' && ch <= 'z')) {
                sb.append(str.charAt(i));
            } else {
                if (sb.length() > 0) {
                    arr.add(sb.toString());
                    sb.delete(0, sb.length());
                }
            }
        }
        
        if(sb.length() > 0) {
            arr.add(sb.toString());
            sb.delete(0, sb.length());
        }
        
        String[] strs = new String[arr.size()];
        strs = arr.toArray(strs);
        return strs;
    }
    public static String[] split(String str, char[] delimeters) {
        List<String> arr = new ArrayList<String>();
        
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<str.length();i++) {
            boolean foundDelimeter = false;
            for(char ch : delimeters) {
                if(str.charAt(i) == ch) {
                    foundDelimeter = true;
                    break;
                }
            }
            
            if(foundDelimeter) {
                if (sb.length() > 0) {
                    arr.add(sb.toString());
                    sb.delete(0, sb.length());
                }
            } else {
                sb.append(str.charAt(i));
            }
        }
        
        if(sb.length() > 0) {
            arr.add(sb.toString());
            sb.delete(0, sb.length());
        }
        
        String[] strs = new String[arr.size()];
        strs = arr.toArray(strs);
        return strs;
    }
    
    public static String[] splitFirstWord(String str) {
        String trimmedStr = str.trim();
        int firstSpaceIndex = trimmedStr.indexOf(" ");
        if (firstSpaceIndex > 0) {
            String firstword = trimmedStr.substring(0, firstSpaceIndex).trim();
            String remains = trimmedStr.substring(firstSpaceIndex + 1).trim();
            return new String[] {firstword, remains};
        }
        return null;
    }
    
    public static String[] splitLastWord(String str) {
        String trimmedStr = str.trim();
        int lastSpaceIndex = trimmedStr.lastIndexOf(" ");
        if (lastSpaceIndex > 0) {
            String remains = trimmedStr.substring(0, lastSpaceIndex).trim();
            String lastword = trimmedStr.substring(lastSpaceIndex + 1).trim();
            return new String[] {remains, lastword};
        }
        return null;
    }
    
    public static String[] splitFirstAndLastWord(String str) {
        String trimmedStr = str.trim();
        int firstSpaceIndex = trimmedStr.indexOf(" ");
        int lastSpaceIndex = trimmedStr.lastIndexOf(" ");
        if (firstSpaceIndex > 0 && lastSpaceIndex > 0 && lastSpaceIndex > firstSpaceIndex) {
            String firstword = trimmedStr.substring(0, firstSpaceIndex).trim();
            String lastword = trimmedStr.substring(lastSpaceIndex + 1).trim();
            String remains = trimmedStr.substring(firstSpaceIndex + 1, lastSpaceIndex).trim();
            return new String[] {firstword, remains, lastword};
        }
        return null;
    }
    
    public static String mergeTwoStringLines(String prev, String next) {
        if(prev == null || prev.equals("")) {
            return next;
        }
        
        boolean endDash = false;
        if(prev.trim().endsWith("-")) {
            endDash = true;
        }
            
        if(endDash) {
            String prevText = prev.trim();
            if(prevText.length() > 1) {
                return prevText.substring(0, prevText.length() - 1) + next.trim();
            } else {
                return next.trim();
            }
        } else {
            return prev.trim() + " " + next.trim();
        }
    }
    
    public static boolean isExtension(String ext) {
        if(ext.length() >= 3 && ext.length() <= 4) {
            return true;
        }
        return false;
    }
    
    public static String removeExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if(dotIndex > 0 && dotIndex < filename.length()) {
            if(isExtension(filename.substring(dotIndex+1))) {
                return filename.substring(0, dotIndex);
            }
            return filename;
        }
        return filename;
    }
    
    public static String removeExtensions(String filename) {
        String filenameTmp;
        String namepart = filename;
        do {
            filenameTmp = namepart;
            namepart = removeExtension(filenameTmp);
        } while(namepart.length() < filenameTmp.length());
        
        return namepart;
    }
    
    public static String getSafeTagName(String name) {
        String safe = name.trim().replaceAll("(\\s|\\.|\\,)", "_").toLowerCase();
        safe = safe.replaceAll("_+$", "");
        safe = safe.replaceAll(":+$", "");
        return safe;
    }
    
    public static String getSafeFileName(String name) {
        //return name.trim().replaceAll("\\s", "_").toLowerCase();
        return name;
    }
    
    private static char matchCloseBrace(char brace) {
        switch(brace) {
            case '{':
                return '}';
            case '[':
                return ']';
            case '(':
                return ')';
            case '<':
                return '>';
        }
        return 0;
    }
    
    public static String removeBraceComments(String name) {
        char brace = 0;
        int depth = 0;
        boolean inComment = false;

        String cleanText = "";
        
        for(int i=0;i<name.length();i++) {
            char ch = name.charAt(i);
            if(ch == '{' || ch == '[' || ch == '(' || ch == '<') {
                if(inComment) {
                    // already started
                    if(ch == brace) {
                        depth++;
                    }
                } else {
                    brace = name.charAt(i);
                    inComment = true;
                    depth = 1;
                }
                
                continue;
            } else if(ch == '}' || ch == ']' || ch == ')' || ch == '>') {
                if(inComment) {
                    if(ch == matchCloseBrace(brace)) {
                        depth--;
                        if(depth <= 0) {
                            inComment = false;
                        }
                    }
                    continue;
                }
            }
            
            if(!inComment) {
                cleanText += ch;
            }
        }
        
        return cleanText;
    }
}
