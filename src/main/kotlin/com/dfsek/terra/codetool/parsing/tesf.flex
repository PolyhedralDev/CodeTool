package com.dfsek.terra.codetool.parsing;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.dfsek.terra.codetool.psi.TesfTypes.*;

%%

%{
  public _TerrascriptLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _TerrascriptLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

NUMBER=[0-9]+(\.[0-9]+)?
STRING=\"([^\"]|\\.)*\"
ID=[a-zA-Z_][a-zA-Z0-9_]*
COMMENT="//"[^\r\n]*
MULTILINE_COMMENT="/"\*([^*]|\*+[^*/])*\*"/"

%%
<YYINITIAL> {
  {WHITE_SPACE}       { return WHITE_SPACE; }

  "if"                { return IF; }
  "else"              { return ELSE; }
  "while"             { return WHILE; }
  "for"               { return FOR; }
  "return"            { return RETURN; }
  "fail"              { return FAIL; }
  "break"             { return BREAK; }
  "continue"          { return CONTINUE; }
  "true"              { return TRUE; }
  "false"             { return FALSE; }
  "str"               { return STR; }
  "num"               { return NUM; }
  "bool"              { return BOOL; }
  "="                 { return EQ; }
  ";"                 { return SEMICOLON; }
  ","                 { return COMMA; }
  "("                 { return LPAREN; }
  ")"                 { return RPAREN; }
  "{"                 { return LBRACE; }
  "}"                 { return RBRACE; }
  "&&"                { return L_AND; }
  "||"                { return L_OR; }
  "!"                 { return L_NOT; }
  "+"                 { return PLUS; }
  "-"                 { return MINUS; }
  "*"                 { return MUL; }
  "/"                 { return DIV; }
  "%"                 { return MOD; }
  "=="                { return EQEQ; }
  "!="                { return NEQ; }
  "<"                 { return LT; }
  ">"                 { return GT; }
  "<="                { return LE; }
  ">="                { return GE; }

  {NUMBER}            { return NUMBER; }
  {STRING}            { return STRING; }
  {ID}                { return ID; }
  {COMMENT}           { return COMMENT; }
  {MULTILINE_COMMENT} { return COMMENT; }

}

[^] { return BAD_CHARACTER; }
