package wyvern.tools.parsing.extensions;

import wyvern.DSL.DSL;
import wyvern.tools.errors.ErrorMessage;
import wyvern.tools.errors.FileLocation;
import wyvern.tools.errors.ToolError;
import wyvern.tools.parsing.*;
import wyvern.tools.rawAST.StringLiteral;
import wyvern.tools.typedAST.core.declarations.ImportDeclaration;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.types.Environment;
import wyvern.tools.util.CompilationContext;
import wyvern.tools.util.Pair;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

/**
 * Created by Ben Chung on 8/9/13.
 */
public class ImportParser implements DeclParser {

	private static LineParser instance;

	public static LineParser getInstance() {
		if (instance == null)
			instance = new ImportParser();
		return instance;
	}
	private ImportParser() {}

	@Override
	public TypedAST parse(TypedAST first, CompilationContext ctx) {
		throw new RuntimeException();
	}

	private static class MutableImportDeclaration extends ImportDeclaration {

		public MutableImportDeclaration(String src, String equivName, FileLocation location) {
			super(src, equivName, null, location);
		}

		public void setEnv(Environment env) {
			super.setDeclEnv(env);
		}
	}

	@Override
	public Pair<Environment, ContParser> parseDeferred(TypedAST first, CompilationContext ctx) {
		final StringLiteral importLiteral = (StringLiteral)ctx.popToken();
		final String importName = importLiteral.data;
		ParseUtils.parseSymbol("as", ctx);
		String alias = ParseUtils.parseSymbol(ctx).name;
		final MutableImportDeclaration declaration = new MutableImportDeclaration(importName, alias, importLiteral.getLocation());
		return new Pair<Environment, ContParser>(declaration.extend(Environment.getEmptyEnvironment()), new RecordTypeParser() {

			private Pair<Environment,ContParser> parserPair;
			private boolean parsed = false;

			@Override
			public TypedAST parse(EnvironmentResolver r) {
				if (parserPair == null) {
					parseTypes(r);
					parseInner(r);
				}
				if (parsed)
					return declaration;
				parsed = true;
				TypedAST result = parserPair.second.parse(r);
				declaration.setAST(result);
				return declaration;
			}

			@Override
			public void parseTypes(EnvironmentResolver r) {
				try {
					parserPair = wyvern.stdlib.Compiler.compilePartial(URI.create(importName), new ArrayList<DSL>());
				} catch (IOException e) {
					ToolError.reportError(ErrorMessage.UNEXPECTED_INPUT, importLiteral);
				}
				if (parserPair.second instanceof RecordTypeParser)
					((RecordTypeParser) parserPair.second).parseTypes(r);
				declaration.setEnv(parserPair.first);

			}

			@Override
			public void parseInner(EnvironmentResolver r) {
				if (parserPair.second instanceof RecordTypeParser)
					((RecordTypeParser) parserPair.second).parseInner(r);
			}
		});
	}
}
