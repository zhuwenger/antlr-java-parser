/*
 * This file is part of antlr-java-parser.
 *
 *     antlr-java-parser is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     antlr-java-parser is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with antlr-java-parser.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.antlrjavaparser.adapter;

import com.github.antlrjavaparser.Java7Parser;
import com.github.antlrjavaparser.api.stmt.CatchClause;

import java.util.LinkedList;
import java.util.List;

public class CatchesContextAdapter implements Adapter<List<CatchClause>, Java7Parser.CatchesContext> {
    public List<CatchClause> adapt(Java7Parser.CatchesContext context) {

        List<CatchClause> catchClauseList = new LinkedList<CatchClause>();
        for (Java7Parser.CatchClauseContext catchClauseContext : context.catchClause()) {
            catchClauseList.add(Adapters.getCatchClauseContextAdapter().adapt(catchClauseContext));
        }

        return catchClauseList;
    }
}