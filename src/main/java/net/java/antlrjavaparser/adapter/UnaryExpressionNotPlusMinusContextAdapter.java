package net.java.antlrjavaparser.adapter;

import net.java.antlrjavaparser.Java7Parser;
import net.java.antlrjavaparser.api.expr.ArrayAccessExpr;
import net.java.antlrjavaparser.api.expr.Expression;
import net.java.antlrjavaparser.api.expr.FieldAccessExpr;
import net.java.antlrjavaparser.api.expr.MethodCallExpr;
import net.java.antlrjavaparser.api.expr.ObjectCreationExpr;
import net.java.antlrjavaparser.api.expr.SuperExpr;
import net.java.antlrjavaparser.api.expr.ThisExpr;
import net.java.antlrjavaparser.api.expr.UnaryExpr;

public class UnaryExpressionNotPlusMinusContextAdapter implements Adapter<Expression, Java7Parser.UnaryExpressionNotPlusMinusContext> {
    public Expression adapt(Java7Parser.UnaryExpressionNotPlusMinusContext context) {

        /*
            unaryExpressionNotPlusMinus
                :   castExpression
                |   primary (selector)* (PLUSPLUS | SUBSUB)?
                ;
         */


        /*
            selector
                :   DOT Identifier (arguments)?
                |   DOT THIS
                |   DOT SUPER superSuffix
                |   innerCreator
                |   LBRACKET expression RBRACKET
                ;

            superSuffix
                :   arguments    // ExplicitConstructorInvocationStmt
                |   DOT (typeArguments)? Identifier (arguments)? // MethodCall
                ;
         */

        /*
            primary
                :    parExpression
                |    THIS (DOT Identifier)* (identifierSuffix)?
                |    Identifier (DOT Identifier)* (identifierSuffix)?
                |    SUPER DOT (typeArguments)? Identifier (arguments)?
                |    literal
                |    creator
                |    primitiveType (LBRACKET RBRACKET)* DOT CLASS
                |    VOID DOT CLASS
                ;

         */

        if (context.castExpression() != null) {
            return Adapters.getCastExpressionContextAdapter().adapt(context.castExpression());
        } else if (context.primary() != null) {

            Expression expression = Adapters.getPrimaryContextAdapter().adapt(context.primary());
            //Expression leftExpression = expression;
            expression = handleSelector(context, expression);

            // Create the Unary Expression
            if (context.PLUSPLUS() != null || context.SUBSUB() != null) {
                UnaryExpr unaryExpr = new UnaryExpr();
                unaryExpr.setExpr(expression);
                UnaryExpr.Operator operator = (context.PLUSPLUS() != null ? UnaryExpr.Operator.posIncrement : UnaryExpr.Operator.posDecrement);
                unaryExpr.setOperator(operator);
                expression = unaryExpr;
            }

            return expression;
        }

        throw new RuntimeException("Unknown UnaryExpressionNotPlusMinusContext type");

    }

    private Expression handleSelector(Java7Parser.UnaryExpressionNotPlusMinusContext context, Expression expression) {

        Expression leftExpression = expression;

        /*
            selector
            locals [int operationType]
                :   DOT Identifier (arguments)?     {$operationType = 1;}
                |   DOT THIS                        {$operationType = 2;}
                |   DOT SUPER superSuffix           {$operationType = 3;}
                |   innerCreator                    {$operationType = 4;}
                |   LBRACKET expression RBRACKET    {$operationType = 5;}
                ;

            superSuffix
                :   DOT (typeArguments)? Identifier (arguments)?
                ;

         */
        if (context.selector() == null || context.selector().size() <= 0) {
            return leftExpression;
        }

        for (Java7Parser.SelectorContext selector : context.selector()) {
            switch (selector.operationType) {
                case 1:
                    if (selector.arguments() != null) {
                        // Method Call
                        MethodCallExpr methodCallExpr = new MethodCallExpr();
                        methodCallExpr.setArgs(Adapters.getArgumentsContextAdapter().adapt(selector.arguments()));
                        methodCallExpr.setName(selector.Identifier().getText());
                        methodCallExpr.setScope(leftExpression);
                        leftExpression = methodCallExpr;
                    } else {
                        // Field access
                        FieldAccessExpr fieldAccessExpr = new FieldAccessExpr();
                        fieldAccessExpr.setScope(leftExpression);
                        fieldAccessExpr.setField(selector.Identifier().getText());
                        leftExpression = fieldAccessExpr;
                    }
                    break;
                case 2:
                    ThisExpr thisExpr = new ThisExpr();
                    thisExpr.setClassExpr(leftExpression);
                    leftExpression = thisExpr;
                    break;
                case 3:
                    throw new UnsupportedOperationException("This should be handled in explicitConstructorInvocation");
                case 4:
                    ObjectCreationExpr objectCreationExpr = Adapters.getInnerCreatorContextAdapter().adapt(selector.innerCreator());
                    objectCreationExpr.setScope(leftExpression);
                    leftExpression = objectCreationExpr;
                    break;
                case 5:
                    ArrayAccessExpr arrayAccessExpr = new ArrayAccessExpr();
                    arrayAccessExpr.setName(leftExpression);
                    arrayAccessExpr.setIndex(Adapters.getExpressionContextAdapter().adapt(selector.expression()));
                    leftExpression = arrayAccessExpr;
                    break;
            }
        }

        return leftExpression;
    }
}
