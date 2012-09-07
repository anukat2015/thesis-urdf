
package urdf.rdf3x;



import java.sql.*;
import java.util.Map;
import java.util.Properties;

// RDF-3X
// (c) 2009 Thomas Neumann. Web site: http://www.mpi-inf.mpg.de/~neumann/rdf3x
//
// This work is licensed under the Creative Commons
// Attribution-Noncommercial-Share Alike 3.0 Unported License. To view a copy
// of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
// or send a letter to Creative Commons, 171 Second Street, Suite 300,
// San Francisco, California, 94105, USA.

public final class Connection implements java.sql.Connection
{
   // The connected process
   private Process process;

   // Constructor
   Connection(Process process) {
      this.process=process;
   }
   // Check if the connection is closed
   void assertOpen() throws SQLException {
      if (process==null)
         throw new SQLException("connection closed");
   }

   // Clear all warnings
   public void clearWarnings() throws SQLException {
      assertOpen();
   }
   // Close the connection
   public void close() throws SQLException {
      if (process!=null) {
         try {
            process.getInputStream().close();
            process.getOutputStream().close();
            process=null;
         } catch (java.io.IOException e) {
            throw new SQLException(e);
         }
      }
   }
   // Commit all changes
   public void commit() throws SQLException {
      assertOpen();
   }
   // Create an array
   public Array createArrayOf(String typeName, Object[] elements) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Create a blob
   public Blob createBlob() throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Create a clob
   public Clob createClob() throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Create a blob
   public NClob createNClob() throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Create a blob
   public SQLXML createSQLXML() throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Create a statement
   public java.sql.Statement createStatement() throws SQLException {
      assertOpen();
      return new Statement(this);
   }
   // Create a statment
   public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Create a statement
   public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Create a struct
   public Struct createStruct(String typeName, Object[] attributes) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Auto-commit?
   public boolean getAutoCommit() throws SQLException { return false; }
   // Catalog name
   public String getCatalog() throws SQLException { return "default"; }
   // Client info
   public Properties getClientInfo() throws SQLException { return new Properties(); }
   // Client info
   public String getClientInfo(String name) throws SQLException { return null; }
   // Holdability
   public int getHoldability() throws SQLException { return ResultSet.CLOSE_CURSORS_AT_COMMIT; }
   // Meta-data
   public DatabaseMetaData getMetaData() throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Trnasaction isolation
   public int getTransactionIsolation() throws SQLException { return Connection.TRANSACTION_SERIALIZABLE; }
   // Type map
   public Map<String,Class<?>> getTypeMap() throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Warning
   public SQLWarning getWarnings() throws SQLException {
      assertOpen();
      return null;
   }
   // Closed?
   public boolean isClosed() throws SQLException { return process==null; }
   // Read-only?
   public boolean isReadOnly() throws SQLException { return true; }
   // Valid connection?
   public boolean isValid(int timeout) throws SQLException { return process!=null; }
   // Construct the native SQL form
   public String nativeSQL(String sql) throws SQLException { return sql; }
   // Prepare a call
   public CallableStatement prepareCall(String sql) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Prepare a call
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Prepare a call
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Prepare a statement
   public PreparedStatement prepareStatement(String sql) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Prepare a statement
   public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Prepare a statement
   public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Prepare a statement
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Prepare a statement
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Prepare a statement
   public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Remove a safebound
   public void releaseSavepoint(Savepoint savepoint) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Rollback
   public void rollback() throws SQLException {
      assertOpen();
      (new Statement(this)).executeQuery("rollback");
   }
   // Rollback to a savepoint
   public void rollback(Savepoint savepoint) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Set auto-commit
   public void setAutoCommit(boolean autoCommit) throws SQLException {
      if (autoCommit)
         throw new SQLFeatureNotSupportedException();
   }
   // Set the catalog
   public void setCatalog(String catalog) throws SQLException {
      if (!"default".equals(catalog))
         throw new SQLFeatureNotSupportedException();
   }
   // Set client info
   public void setClientInfo(Properties properties) {}
   // Set client info
   public void setClientInfo(String name, String value) {}
   // Set holdability
   public void setHoldability(int holdability) throws SQLException {
      if (holdability!=ResultSet.CLOSE_CURSORS_AT_COMMIT)
         throw new SQLFeatureNotSupportedException();
   }
   // Set read-only
   public void setReadOnly(boolean readOnly) throws SQLException {
      if (!readOnly)
         throw new SQLFeatureNotSupportedException();
   }
   // Set a save point
   public Savepoint setSavepoint() throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Set a save point
   public Savepoint setSavepoint(String name) throws SQLException { throw new SQLFeatureNotSupportedException(); }
   // Set the isolation level
   public void setTransactionIsolation(int level) {}
   // Set the type map
   public void	setTypeMap(Map<String,Class<?>> map) throws SQLException { throw new SQLFeatureNotSupportedException(); }

   /// Wrapper?
   public boolean isWrapperFor(Class<?> iface) { return false; }
   /// Unwrap
   public <T> T	unwrap(Class<T> iface) throws SQLException { throw new SQLException(); }

   /// Send a line to the server
   void writeLine(String s) throws SQLException
   {
      try {
         java.io.OutputStream out=process.getOutputStream();
         for (int index=0;index<s.length();index++) {
            char c=s.charAt(index);
            if (c<0x80) {
               byte b1=(byte)c;
               if ((b1==' ')||(b1=='\n')||(b1=='\\'))
                  out.write('\\');
               out.write(b1);
            } else if (c<0x800) {
               byte b1=(byte)(0xc0 | (0x1f & (c >> 6)));
               byte b2=(byte)(0x80 | (0x3f & c));
               if ((b1==' ')||(b1=='\n')||(b1=='\\'))
                  out.write('\\');
               out.write(b1);
               if ((b2==' ')||(b2=='\n')||(b2=='\\'))
                  out.write('\\');
               out.write(b2);
            } else {
               byte b1=(byte)(0xe0 | (0x0f & (c >> 12)));
               byte b2=(byte)(0x80 | (0x3f & (c >>  6)));
               byte b3=(byte)(0x80 | (0x3f & c));
               if ((b1==' ')||(b1=='\n')||(b1=='\\'))
                  out.write('\\');
               out.write(b1);
               if ((b2==' ')||(b2=='\n')||(b2=='\\'))
                  out.write('\\');
               out.write(b2);
               if ((b3==' ')||(b3=='\n')||(b3=='\\'))
                  out.write('\\');
               out.write(b3);
            }
         }
         out.write('\n');
         out.flush();
      } catch (java.io.IOException e) {
         throw new SQLException(e);
    	  //writeLine(s); // retry
      }
   }
   /// Send a line to the server
   void writeResultLine(String[] cols) throws SQLException
   {
      try {
         java.io.OutputStream out=process.getOutputStream();
         if (cols==null) {
            // End marker
            out.write('\\');
            out.write('.');
            out.write('\n');
            out.flush();
            return;
         }
         for (int index=0;index<cols.length;index++) {
            String s=cols[index];
            if (index>0) out.write(' ');
            for (int index2=0;index2<s.length();index2++) {
               char c=s.charAt(index2);
               if (c<0x80) {
                  byte b1=(byte)c;
                  if ((b1==' ')||(b1=='\n')||(b1=='\\'))
                     out.write('\\');
                  out.write(b1);
               } else if (c<0x800) {
                  byte b1=(byte)(0xc0 | (0x1f & (c >> 6)));
                  byte b2=(byte)(0x80 | (0x3f & c));
                  if ((b1==' ')||(b1=='\n')||(b1=='\\'))
                     out.write('\\');
                  out.write(b1);
                  if ((b2==' ')||(b2=='\n')||(b2=='\\'))
                     out.write('\\');
                  out.write(b2);
               } else {
                  byte b1=(byte)(0xe0 | (0x0f & (c >> 12)));
                  byte b2=(byte)(0x80 | (0x3f & (c >>  6)));
                  byte b3=(byte)(0x80 | (0x3f & c));
                  if ((b1==' ')||(b1=='\n')||(b1=='\\'))
                     out.write('\\');
                  out.write(b1);
                  if ((b2==' ')||(b2=='\n')||(b2=='\\'))
                     out.write('\\');
                  out.write(b2);
                  if ((b3==' ')||(b3=='\n')||(b3=='\\'))
                     out.write('\\');
                  out.write(b3);
               }
            }
         }
         out.write('\n');
      } catch (java.io.IOException e) {
         throw new SQLException(e);
      }
   }
   /// Read the next line
   String readLine() throws SQLException
   {
      StringBuilder builder=new StringBuilder();
      try {
         java.io.InputStream in=process.getInputStream();
         while (true) {
            int b1=in.read();
            if (b1==-1)
               throw new SQLException("connection closed");
            if (b1=='\n')
               return builder.toString();
            if (b1=='\\')
               b1=in.read();
            char c;
            if (b1<128) {
               c=(char)b1;
            } else if ((b1&0xE0)==0xC0) {
               int b2=in.read();
               if (b2=='\\') b2=in.read();
               c=(char)(((b1&0x1F)<<6)|(b2&0x3F));
            } else if ((b1&0xF0)==0xE0) {
               int b2=in.read();
               if (b2=='\\') b2=in.read();
               int b3=in.read();
               if (b3=='\\') b3=in.read();
               c=(char)(((b1&0x0F)<<12)|((b2&0x3F)<<6)|(b3&0x3F));
            } else {
               c='?'; // Invalid utf8!
            }
            builder.append(c);
         }
      } catch (java.io.IOException e) {
         throw new SQLException(e);
      }
   }

   /// Read the next result line
   String[] readResultLine() throws SQLException
   {
      java.util.ArrayList<String> result=new java.util.ArrayList<String>();
      StringBuilder builder=new StringBuilder();
      try {
         java.io.InputStream in=process.getInputStream();
         while (true) {
            int b1=in.read();
            if (b1==-1)
               throw new SQLException("connection closed");
            if (b1=='\n') {
               result.add(builder.toString());
               return result.toArray(new String[0]);
            }
            if (b1==' ') {
               result.add(builder.toString());
               builder.setLength(0);
               continue;
            }
            if (b1=='\\') {
               b1=in.read();
               if (b1=='.') { // End marker
                  if (in.read()!='\n')
                     throw new SQLException("invalid data");
                  return null;
               }
            }
            char c;
            if (b1<128) {
               c=(char)b1;
            } else if ((b1&0xE0)==0xC0) {
               int b2=in.read();
               if (b2=='\\') b2=in.read();
               c=(char)(((b1&0x1F)<<6)|(b2&0x3F));
            } else if ((b1&0xF0)==0xE0) {
               int b2=in.read();
               if (b2=='\\') b2=in.read();
               int b3=in.read();
               if (b3=='\\') b3=in.read();
               c=(char)(((b1&0x0F)<<12)|((b2&0x3F)<<6)|(b3&0x3F));
            } else {
               c='?'; // Invalid utf8!
            }
            builder.append(c);
         }
      } catch (java.io.IOException e) {
         throw new SQLException(e);
      }
   }
}
