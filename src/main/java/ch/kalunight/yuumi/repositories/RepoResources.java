package ch.kalunight.yuumi.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ch.kalunight.yuumi.model.dto.DTO;
import ch.kalunight.yuumi.translation.LanguageManager;
import net.dv8tion.jda.api.entities.MessageChannel;

public class RepoResources {

	public static final String DB_USERNAME = "yuumiadmin";

	private static HikariDataSource dataSource;

	private static final Logger logger = LoggerFactory.getLogger(RepoResources.class);

	private RepoResources() {
		//hide Repo Ressources
	}

	public static void setupDatabase(String password, String url) {
		PGSimpleDataSource source = new PGSimpleDataSource();
		source.setURL(url);
		source.setDatabaseName("yuumi");
		source.setUser(DB_USERNAME);
		source.setPassword(password);

		HikariConfig config = new HikariConfig();
		config.setDataSource(source);
		config.setAutoCommit(true);

		dataSource = new HikariDataSource(config);
	}

	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public static void closeResultSet(ResultSet result) throws SQLException {
		if(result != null && !result.isClosed()) {
			result.close();
		}
	}

	public static void sqlErrorReport(MessageChannel channel, DTO.Server server, SQLException e) {
		logger.error("SQL issue when updating option", e);
		channel.sendMessage(LanguageManager.getText(server.serv_language, "errorSQLPleaseReport")).complete();
	}

	public static void shutdownDB() {
		dataSource.close();
	}

	public static HikariDataSource getDataSource() {
		return dataSource;
	}

}
