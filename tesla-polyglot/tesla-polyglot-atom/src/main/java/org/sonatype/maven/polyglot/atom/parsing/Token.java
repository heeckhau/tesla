package org.sonatype.maven.polyglot.atom.parsing;

import java.util.HashMap;
import java.util.Map;

/**
 * Taken from the Loop programming language compiler pipeline.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Token {
  public final String value;
  public final Kind kind;

  public Token(String value, Kind kind) {
    this.value = value;
    this.kind = kind;
  }

  public static enum Kind {
    IDENT,
    TYPE_IDENT,
    INTEGER,
    STRING,
    REGEX,
    DOT,

    COLON,
    MINUS,
    DIVIDE,
    STAR,
    MODULUS,

    COMMA,

    AT,
    ARROW,
    HASHROCKET,

    // Comparison operators
    EQUALS,
    LEFT_WAVE,
    GREATER,
    LEQ,
    GEQ,

    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    LBRACKET,
    RBRACKET,

    // keywords
    REQUIRE,
    SRCS,
    SCM,

    PROJECT,
    REPOSITORIES,
    OR,
    AND,
    NOT,
    FOR,
    IN,
    IF,
    DEPS,
    ID,
    PARENT,
    UNLESS,
    WHERE,

    WHEN,

    // specials
    EOL,
    INDENT, 
    DASH, 
    PACKAGING, 
    OVERRIDES, 
    PROPS,
    MODULES, 
    PLUGINS;
    //DOLLAR, 
    //PROJECT_DOT_VERSION;

    private static final Map<String, Kind> TOKEN_MAP = new HashMap<String, Kind>();

    static {
      // can we optimize with chars?
      TOKEN_MAP.put("@", AT);
      TOKEN_MAP.put(".", DOT);
      TOKEN_MAP.put("-", DASH);
      TOKEN_MAP.put(":", COLON);
      TOKEN_MAP.put("*", STAR);
      TOKEN_MAP.put(",", COMMA);
      TOKEN_MAP.put("->", ARROW);

      TOKEN_MAP.put("<=", LEQ);
      TOKEN_MAP.put(">=", GEQ);
      TOKEN_MAP.put("<<", LEFT_WAVE);
      TOKEN_MAP.put(">", GREATER);
      TOKEN_MAP.put("=>", HASHROCKET);

      TOKEN_MAP.put("(", LPAREN);
      TOKEN_MAP.put(")", RPAREN);
      //TOKEN_MAP.put("$", DOLLAR);      
      //TOKEN_MAP.put("{", LBRACE);
      //TOKEN_MAP.put("}", RBRACE);
      TOKEN_MAP.put("[", LBRACKET);
      TOKEN_MAP.put("]", RBRACKET);
      TOKEN_MAP.put("\n", EOL);

      TOKEN_MAP.put("id", ID);
      TOKEN_MAP.put("inherit", PARENT);
      TOKEN_MAP.put("packaging", PACKAGING);
      TOKEN_MAP.put("properties", PROPS);
      TOKEN_MAP.put("deps", DEPS);
      TOKEN_MAP.put("overrides", OVERRIDES);
      TOKEN_MAP.put("repositories", REPOSITORIES);
      TOKEN_MAP.put("project", PROJECT);
      TOKEN_MAP.put("srcs", SRCS);
      TOKEN_MAP.put("scm", SCM);      
      TOKEN_MAP.put("modules", MODULES);      
      TOKEN_MAP.put("plugins", PLUGINS );      

      TOKEN_MAP.put("require", REQUIRE);


      TOKEN_MAP.put("||", OR);
      TOKEN_MAP.put("or", OR);
      TOKEN_MAP.put("&&", AND);
      TOKEN_MAP.put("and", AND);
      TOKEN_MAP.put("!", NOT);
      TOKEN_MAP.put("not", NOT);
    }

    /**
     * from token text, determines kind.
     */
    public static Kind determine(String value) {
      char first = value.charAt(0);

      if (first == '"' || first == '\'')
        return STRING;

      Kind knownKind = TOKEN_MAP.get(value);
      if (null != knownKind)
        return knownKind;

      return IDENT;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Token token = (Token) o;

    return (kind == token.kind)
        && !(value != null ? !value.equals(token.value) : token.value != null);

  }

  @Override
  public int hashCode() {
    int result = value != null ? value.hashCode() : 0;
    result = 31 * result + (kind != null ? kind.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Token{" +
        "value='" + value + '\'' +
        ", kind=" + kind +
        '}';
  }
}