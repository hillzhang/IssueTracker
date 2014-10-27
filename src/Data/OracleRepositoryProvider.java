package Data;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTimeoutException;
import java.util.Vector;

import oracle.jdbc.pool.OracleDataSource;
import Business.Issue;
import Presentation.IRepositoryProvider;

/**
 * Encapsulates create/read/update/delete operations to Oracle database
 * 
 * @author matthewsladescu
 * 
 */
public class OracleRepositoryProvider implements IRepositoryProvider {
	// connection parameters - ENTER YOUR LOGIN AND PASSWORD HERE
	private final String userid = "lzha4956";
	private final String passwd = "linshan1206";
	private final String database = "oracle12.it.usyd.edu.au:1521:COMP5138";
	private Connection conn;

	/**
	 * Update the details for a given issue
	 * 
	 * @param issue
	 *            : the issue for which to update details
	 */
	@Override
	public void updateIssue(Issue issue) {
		String title = "", description = "";
		int creator, resolver, verifier,projectID, versionnumber, versioncheck;
		CallableStatement updateissue;
		CallableStatement issueversion;
		projectID = issue.getId();
		title = issue.getTitle()== null ? "" : issue.getTitle();
		description = issue.getDescription() == null ? "" : issue.getDescription();
		creator =(Integer) (issue.getCreator() == null ? 0 : issue.getCreator());
		resolver = (Integer) (issue.getResolver() == null ? 0 : issue.getResolver());
		verifier = (Integer) (issue.getVerifier() == null ? 0 : issue.getVerifier());
		versionnumber = issue.getIssueversion();
		try {
			openConnection();
			System.out.println("Updating.......");
			conn.setAutoCommit(false);
			issueversion = conn.prepareCall("call Issueversion_check(?,?)");
			issueversion.setInt(1, projectID);
			issueversion.registerOutParameter(2, oracle.jdbc.OracleTypes.INTEGER);
			issueversion.execute();
			versioncheck = (Integer)issueversion.getObject(2);
			issueversion.close();
			if (versionnumber == versioncheck){
				try {
					updateissue = conn.prepareCall("call updateissue(?,?,?,?,?,?)");
					updateissue.setInt(1, projectID);
					updateissue.setString(2, title);
					updateissue.setInt(3, creator);
					
					if (resolver == 0){
						updateissue.setNull(4, oracle.jdbc.OracleTypes.INTEGER);
					} else {
						updateissue.setInt(4, resolver);
					}
					if (verifier == 0){
						updateissue.setNull(5, oracle.jdbc.OracleTypes.INTEGER);
						
					} else {
						updateissue.setInt(5, verifier);
					}
					
					updateissue.setString(6, description);
					updateissue.execute();
					updateissue.close();
					conn.commit();
					System.out.println("Update successfull.......");
				} catch (SQLIntegrityConstraintViolationException e) {
					System.err.println("Update failed:");
					System.err.println("Violated a constraint(check your creator or resolver or verifier ID)!!");
					// TODO Auto-generated catch block
				} catch (SQLTimeoutException e){
					System.err.println("Update failed:");
					System.err.println("Operation timed out!!");
					
				}catch (SQLException e) {
					System.err.println("Update failed:");
					System.out.println("Other problems!!!!!");
				} 
			} else {
				System.err.println("Update failed:");
				System.err.println("You cannot update it now!! you can choose to re-load the issue and return to your edits for the issue");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("Update failed:");
			System.err.println(e.getMessage());
		} finally {
			closeConnection();
		}
	}

	/**
	 * Find the issues associated in some way with a user Issues which have the
	 * id parameter below in any one or more of the creator, resolver, or
	 * verifier fields should be included in the result
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Vector<Issue> findUserIssues(int id) {
		Vector<Issue> issueList = new Vector<Issue>();
		CallableStatement findissue;
		int userid = id;
		ResultSet userissue = null;
		try {
			openConnection();
			System.out.println("Listing issues.....");
			conn.setAutoCommit(false);
			findissue = conn.prepareCall("{call UserIssues(?,?)}");
			findissue.setInt(1, userid);
			findissue.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
			findissue.execute();
			userissue = (ResultSet) findissue.getObject(2);
			while (userissue.next()) {
				Issue issue = new Issue();
				issue.setId(userissue.getInt("ID"));
				issue.setTitle(userissue.getString("TITLE"));
				issue.setDescription(userissue.getString("DESCRIPTION"));
				issue.setCreator((userissue.getInt("CREATOR") == 0 ? null : userissue.getInt("CREATOR")));
				issue.setResolver((userissue.getInt("RESOLVER") == 0 ? null : userissue.getInt("RESOLVER")));
				issue.setVerifier((userissue.getInt("VERIFIER") == 0 ? null : userissue.getInt("VERIFIER")));
				issue.setIssueversion(userissue.getInt("ISSUEVERSION"));
				issueList.add(issue);
			}
			findissue.close();
			conn.commit();
			System.out.println("Listing issues successfull.....");
		} catch (Exception e) {
			// TODO Auto-generated catch block 
			try { 
				conn.rollback();
			} catch (SQLException e1) {
				System.err.println("Error: " + e.getMessage());
			}
		} finally {
			closeConnection();
		}
		return issueList;
	}

	/**
	 * Add the details for a new issue to the database
	 * 
	 * @param issue
	 *            : the new issue to add
	 */
	@Override
	public void addIssue(Issue issue) {
		String title = "", description = "";
		int creator, resolver, verifier,success;
		title = issue.getTitle()== null ? "" : issue.getTitle();
		description = issue.getDescription() == null ? "" : issue.getDescription();
		creator = issue.getCreator();
		resolver = (Integer) (issue.getResolver() == null ? 0 : issue.getResolver());
		verifier = (Integer) (issue.getVerifier() == null ? 0 : issue.getVerifier());
		CallableStatement addissue;
		try {
			openConnection();
			System.out.println("Adding issues......");
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			addissue = conn.prepareCall("call addissue(?,?,?,?,?,?)");
			
			addissue.setString(1, title);
			addissue.setInt(2, creator);
			if (resolver == 0){
				addissue.setNull(3, oracle.jdbc.OracleTypes.INTEGER);
			} else {
				addissue.setInt(3, resolver);
			}
			if (verifier == 0){
				addissue.setNull(4, oracle.jdbc.OracleTypes.INTEGER);
				
			} else {
				addissue.setInt(4, verifier);
			}
			addissue.setString(5, description);
			addissue.registerOutParameter(6, oracle.jdbc.OracleTypes.INTEGER);
			addissue.execute();
			success = (Integer)addissue.getObject(6);
			if (success == 1){
				addissue.close();
				conn.commit();
				System.out.println("Add issues successfull.....");
				} else {
					addissue.close();
					conn.rollback();
				}
		} catch (SQLIntegrityConstraintViolationException e) {
			System.err.println("Add issue failed:");
			System.err.println("Violated a constraint(check your creator or resolver or verifier ID)!!");
		} catch (SQLTimeoutException e){
			System.err.println("Add issue failed:");
			System.err.println("Operation timed out!!");
		} catch (SQLException e){
			System.err.println("Add issue failed:");
			System.out.println("Error: " + e.getMessage());
		} finally {
			closeConnection();
		}
	}

	/**
	 * Given an expression searchString like myFirst words|my second words this
	 * method should return any issues associated with a user based on userId
	 * that either: contain 1 or more of the phrases separated by the '|'
	 * character in the issue title OR contain 1 or more of the phrases
	 * separated by the '|' character in the issue description OR
	 * 
	 * @param searchString
	 *            : the searchString to use for finding issues in the database
	 *            based on the issue titles and descriptions. searchString may
	 *            either be a single phrase, or a phrase separated by the '|'
	 *            character. The searchString is used as described above to find
	 *            matching issues in the database.
	 * @param userId
	 *            : used to first find issues associated with userId on either
	 *            one or more of the creator/resolver/verifier fields. Once a
	 *            user's issues are identified, the search would then take place
	 *            on the user's associated issues.
	 * @return
	 */
	@Override
	public Vector<Issue> findIssueBasedOnExpressionSearchedOnTitleAndDescription(
			String searchString, int userId) {

		Vector<Issue> issueList = new Vector<Issue>();
		CallableStatement findissue;
		ResultSet userissue = null;
		String namecheck = "";
		String titlecheck = "";
		if (searchString.startsWith("@")) {
			String[] string = searchString.split("@");
			if (string.length == 2){
				namecheck = string[1];
			} else {
				namecheck = string[1];
				titlecheck = string[2];
			}
		}
		else {
			titlecheck = searchString;
		}
		try {
			openConnection();
			System.out.println("Finding issues.....");
			conn.setAutoCommit(false);
			findissue = conn.prepareCall("{call UserIssues(?,?)}");
			findissue.setInt(1, userId);
			findissue.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
			findissue.execute();
			userissue = (ResultSet)findissue.getObject(2);
			while (userissue.next()) {
				CallableStatement searchcheck ;
				int success = 0;
				Issue issue = new Issue();
				issue.setId(userissue.getInt("ID"));
				issue.setTitle(userissue.getString("TITLE"));
				issue.setDescription(userissue.getString("DESCRIPTION"));
				issue.setCreator((userissue.getInt("CREATOR") == 0 ? null : userissue.getInt("CREATOR")));
				issue.setResolver((userissue.getInt("RESOLVER") == 0 ? null : userissue.getInt("RESOLVER")));
				issue.setVerifier((userissue.getInt("VERIFIER") == 0 ? null : userissue.getInt("VERIFIER")));
				issue.setIssueversion(userissue.getInt("ISSUEVERSION"));
				
				searchcheck = conn.prepareCall("{call FindUserIssue(?,?,?,?,?)}");
				searchcheck.setString(1, namecheck);
				searchcheck.setInt(2, userissue.getInt("CREATOR"));
				searchcheck.setInt(3,userissue.getInt("RESOLVER"));
				searchcheck.setInt(4, userissue.getInt("VERIFIER"));
				searchcheck.registerOutParameter(5, oracle.jdbc.OracleTypes.INTEGER);
				searchcheck.execute();
				success = (Integer)searchcheck.getObject(5);
				if (success == 1) {
					boolean validtitle = false;
					String[] string = titlecheck.split("\\|");
					for (int i = 0; i < string.length; i ++){
						if (userissue.getString("TITLE").indexOf(string[i]) >= 0 || 
								userissue.getString("DESCRIPTION").indexOf(string[i]) >= 0){
							validtitle = true;
						}
					}
					if (validtitle) {
						issueList.add(issue);
					}
				}
			}
			conn.commit();
			System.out.println("Finding issues successfull.....");
		} catch (SQLTimeoutException e){
			System.err.println("Find issues failed:");
			System.err.println("Operation timed out!!");
		} catch (SQLException e){
			System.out.println("Find issues failed:");
			System.out.println("Error: " + e.getMessage());
		} finally {
			closeConnection();
		}
		return issueList;
	}

	public boolean openConnection() {
		boolean retval = true;
		if (conn != null)
			System.err.println("You are already connected to Oracle; no second connection is needed!");
		else {
			if (connectToDatabase())
				System.out.println("You successfully connected to Oracle.");
			else {
				System.out.println("Oops - something went wrong.");
				retval = false;
			}
		}

		return retval;
	}

	/**
	 * close the database connection again
	 */
	public void closeConnection() {
		if (conn == null)
			System.err.println("You are not connected to Oracle!");
		else
			try {
				conn.close(); // close the connection again after usage!
				conn = null;
				System.out.println("Disconnected to Oracle.");
			} catch (SQLException sql_ex) { /* error handling */
				System.out.println(sql_ex);
			}
	}

	public boolean connectToDatabase() {
		try {
			/* connect to the database */
			conn = DriverManager.getConnection("jdbc:oracle:thin:@" + database,
					userid, passwd);
			return true;
		} catch (SQLException sql_ex) {
			/* error handling */
			System.out.println(sql_ex);
			return false;
		}
	}

}
