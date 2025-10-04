package com.dfsek.terra.codetool.formatting

import com.dfsek.terra.codetool.TerrascriptLanguage
import com.dfsek.terra.codetool.psi.TesfTypes
import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class TerrascriptBlock(
    private val node: ASTNode,
    private val alignment: Alignment?,
    private val indent: Indent?,
    private val wrap: Wrap?,
    private val settings: CodeStyleSettings
) : ASTBlock {
    private val spacingBuilder = createSpacingBuilder(settings)

    override fun getNode(): ASTNode = node
    override fun getTextRange(): TextRange = node.textRange
    override fun getWrap(): Wrap? = wrap
    override fun getIndent(): Indent? = indent
    override fun getAlignment(): Alignment? = alignment
    override fun isLeaf(): Boolean = node.firstChildNode == null
    override fun isIncomplete(): Boolean = false

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        val parentType = node.elementType
        if (parentType == TesfTypes.BLOCK) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }
        return ChildAttributes(Indent.getNoneIndent(), null)
    }

    override fun getSubBlocks(): List<Block> {
        val blocks = ArrayList<Block>()
        var child = node.firstChildNode
        while (child != null) {
            if (child.elementType != TokenType.WHITE_SPACE && child.textLength > 0) {
                val block = TerrascriptBlock(
                    child,
                    null,
                    calcIndent(child),
                    null,
                    settings
                )
                blocks.add(block)
            }
            child = child.treeNext
        }
        return blocks
    }

    private fun getPrevNonWhitespaceSibling(child: ASTNode): ASTNode? {
        var prev = child.treePrev
        while (prev != null && prev.elementType == TokenType.WHITE_SPACE) {
            prev = prev.treePrev
        }
        return prev
    }

    private fun getFirstRelevantSibling(parentNode: ASTNode): ASTNode? {
        var current = parentNode.firstChildNode
        while (current != null && current.elementType == TokenType.WHITE_SPACE) {
            current = current.treeNext
        }
        return current
    }

    private fun isExpressionElementType(elementType: IElementType?): Boolean {
        return elementType != null && expressionContainerTypes.contains(elementType)
    }


    private fun calcIndent(child: ASTNode): Indent? {
        val parentNode = node
        val parentType = parentNode.elementType
        val childType = child.elementType

        if (parentType == TesfTypes.BLOCK) {
            if (childType != TesfTypes.LBRACE && childType != TesfTypes.RBRACE) {
                return Indent.getNormalIndent()
            }
        } else if ((parentType == TesfTypes.IF_STATEMENT ||
                    parentType == TesfTypes.FOR_LOOP ||
                    parentType == TesfTypes.WHILE_LOOP) &&
            childType == TesfTypes.BLOCK
        ) {
            return Indent.getNormalIndent()
        } else if (isExpressionElementType(parentType)) {
            val firstRelevantSiblingOfParent = getFirstRelevantSibling(parentNode)
            if (child != firstRelevantSiblingOfParent) {
                val prevNonWsSibling = getPrevNonWhitespaceSibling(child)
                return if (prevNonWsSibling?.elementType == TesfTypes.LPAREN && childType != TesfTypes.RPAREN) {
                    Indent.getNormalIndent()
                } else if (childType == TesfTypes.RPAREN && prevNonWsSibling != null && prevNonWsSibling.elementType != TesfTypes.LPAREN) {
                    Indent.getNormalIndent()
                } else {
                    Indent.getNormalIndent()
                }
            }
            return Indent.getNoneIndent()
        }
        return Indent.getNoneIndent()
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        val child1AstBlock = child1 as? ASTBlock
        val child2AstBlock = child2 as? ASTBlock

        if (child1AstBlock != null && child2AstBlock != null) {
            val leftNode = child1AstBlock.node
            if (leftNode != null) {
                if (leftNode.elementType == TesfTypes.MINUS || leftNode.elementType == TesfTypes.PLUS) {
                    var prevPsiSibling = leftNode.treePrev
                    while (prevPsiSibling != null && prevPsiSibling.elementType == TokenType.WHITE_SPACE) {
                        prevPsiSibling = prevPsiSibling.treePrev
                    }

                    // The operator is unary if it's at the start of a line/block,
                    // or if the token preceding it is NOT an operand (e.g., it's another operator, LPAREN, etc.)
                    val isUnary = prevPsiSibling == null ||
                            !operandTokens.contains(prevPsiSibling.elementType) ||
                            prevPsiSibling.elementType == TesfTypes.LPAREN ||
                            binaryAndAmbiguousOperators.contains(prevPsiSibling.elementType) ||
                            prevPsiSibling.elementType == TesfTypes.EQ ||
                            prevPsiSibling.elementType == TesfTypes.COMMA

                    if (isUnary) {
                        return Spacing.createSpacing(0, 0, 0, false, 0)
                    }
                }
            }
        }

        return spacingBuilder.getSpacing(this, child1, child2)
    }

    companion object {
        private val exclusivelyUnaryPrefixOperators = TokenSet.create(
            TesfTypes.L_NOT
        )

        private val binaryAndAmbiguousOperators = TokenSet.create(
            TesfTypes.PLUS, TesfTypes.MINUS, TesfTypes.MUL, TesfTypes.DIV, TesfTypes.MOD,
            TesfTypes.EQ, TesfTypes.EQEQ, TesfTypes.NEQ,
            TesfTypes.LT, TesfTypes.GT, TesfTypes.LE, TesfTypes.GE,
            TesfTypes.L_AND, TesfTypes.L_OR
        )

        val operandTokens = TokenSet.create(
            TesfTypes.ID,
            TesfTypes.NUMBER,
            TesfTypes.RPAREN,
            TesfTypes.STRING,
            TesfTypes.TRUE,
            TesfTypes.FALSE,
            TesfTypes.LITERAL,
            TesfTypes.VARIABLE_USAGE,
            TesfTypes.CALL_EXPRESSION
        )

        val expressionContainerTypes: TokenSet = TokenSet.create(
            TesfTypes.EXPRESSION,
            TesfTypes.ARGUMENT_LIST
        )

        private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
            val builder = SpacingBuilder(settings, TerrascriptLanguage)

            builder.after(exclusivelyUnaryPrefixOperators).spaceIf(false)

            builder.around(binaryAndAmbiguousOperators).spaceIf(true)

            builder.after(TesfTypes.COMMA).spaceIf(true)
                .afterInside(TesfTypes.SEMICOLON, TesfTypes.FOR_LOOP).spaceIf(true)
                .before(TesfTypes.LBRACE).spaceIf(true)
                .after(TesfTypes.LBRACE).lineBreakInCode()
                .before(TesfTypes.RBRACE).lineBreakInCode()
                .between(TesfTypes.IF_STATEMENT, TesfTypes.ELSE_BLOCK).spaceIf(true)
                .beforeInside(TesfTypes.BLOCK, TesfTypes.IF_STATEMENT).spaceIf(true)
                .around(TesfTypes.ELSE).spaceIf(true)
                .before(TesfTypes.ELSE).spaceIf(true)
                .after(TesfTypes.LPAREN).spaceIf(false) // No space after (
                .before(TesfTypes.RPAREN).spaceIf(false) // No space before )
                .between(TesfTypes.IF, TesfTypes.LPAREN).spaceIf(true)
                .between(TesfTypes.FOR, TesfTypes.LPAREN).spaceIf(true)
                .between(TesfTypes.WHILE, TesfTypes.LPAREN).spaceIf(true)
                .between(TesfTypes.SEMICOLON, TesfTypes.EXPRESSION).lineBreakInCode()
                .between(TesfTypes.SEMICOLON, TesfTypes.VARIABLE_DECLARATION).lineBreakInCode()
                .between(TesfTypes.SEMICOLON, TesfTypes.IF_STATEMENT).lineBreakInCode()
                .between(TesfTypes.SEMICOLON, TesfTypes.FOR_LOOP).lineBreakInCode()
                .between(TesfTypes.SEMICOLON, TesfTypes.WHILE_LOOP).lineBreakInCode()
                .after(TesfTypes.BLOCK).lineBreakInCode()

            return builder
        }
    }
}