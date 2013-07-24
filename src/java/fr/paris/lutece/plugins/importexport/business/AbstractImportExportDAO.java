package fr.paris.lutece.plugins.importexport.business;

import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;


/**
 * Abstract DAO to manage imports and exports
 */
public abstract class AbstractImportExportDAO
{
    protected static final String SQL_QUERY_GET_PRIMARY_KEYS = " SELECT column_name, data_type FROM information_schema.columns WHERE table_name = ? ";

    private static final String CONSTANT_SQL_INT = "int";
    private static final String CONSTANT_SQL_LONG = "bigint";
    private static final String CONSTANT_SQL_DOUBLE = "double";
    private static final String CONSTANT_SQL_REAL = "real";
    private static final String CONSTANT_SQL_VARCHAR = "varchar";
    private static final String CONSTANT_SQL_CHARACTER = "character";
    private static final String CONSTANT_SQL_TEXT = "text";
    private static final String CONSTANT_SQL_TIMESTAMP = "timestamp";
    private static final String CONSTANT_SQL_DATE = "date";
    private static final String CONSTANT_SQL_BYTE = "byte";
    private static final String CONSTANT_SQL_BLOB = "blob";

    private static final String ERROR_MESSAGE_COLUMN_NOT_FOUND = "importexport.import_data.errors.columnNotFound";

    /**
     * Get the list of columns of a table matching a given list of names. The
     * result list is sorted in the same order as the list of columns name
     * @param listColumnNames The list of names of columns to consider
     * @param strTableName The name of the database table
     * @param plugin The plugin to use the pool of
     * @param locale The locale to display errors in
     * @return The list of columns, or an empty list if no columns was found.
     * @throws AppException If the table does not contain any column whithin a
     *             name of the columns name list
     */
    protected static List<TableColumn> getTableColumns( List<String> listColumnNames, String strTableName,
            Plugin plugin, Locale locale ) throws AppException
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_GET_PRIMARY_KEYS, plugin );
        daoUtil.setString( 1, strTableName );
        daoUtil.executeQuery( );
        List<TableColumn> listColumns = new ArrayList<TableColumn>( listColumnNames.size( ) );
        String strPrimaryKeyName = listColumnNames.get( 0 );
        while ( daoUtil.next( ) )
        {
            String strColumnName = daoUtil.getString( 1 );
            if ( listColumnNames.contains( strColumnName ) )
            {
                String strColumnType = daoUtil.getString( 2 );
                TableColumn tableColumn = new TableColumn( strColumnName, StringUtils.equals( strPrimaryKeyName,
                        strColumnName ), getJavaTypeFromSqlString( strColumnType ) );
                listColumns.add( tableColumn );
            }
        }
        daoUtil.free( );

        // We now sort elements of the list in the original order
        List<TableColumn> listColumnsSorted = new ArrayList<TableColumn>( listColumns.size( ) );
        for ( String strColumnName : listColumnNames )
        {
            Iterator<TableColumn> iterator = listColumns.iterator( );
            TableColumn tableColumn;
            try
            {
                while ( ( tableColumn = iterator.next( ) ) != null )
                {
                    if ( StringUtils.equals( strColumnName, tableColumn.getColumnName( ) ) )
                    {
                        listColumnsSorted.add( tableColumn );
                        iterator.remove( );
                        break;
                    }
                }
            }
            catch ( NoSuchElementException e )
            {
                throw new AppException( I18nService.getLocalizedString( ERROR_MESSAGE_COLUMN_NOT_FOUND, locale ) );
            }
        }

        return listColumnsSorted;
    }

    /**
     * Get the list of columns names from the database
     * @param strTableName The name of the table to get columns name from
     * @param plugin The plugin to use the pool of
     * @return The list of names of columns of the database, or an empty list if
     *         no columns was found
     */
    public static List<String> getTableColumnsNames( String strTableName, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_GET_PRIMARY_KEYS, plugin );
        daoUtil.setString( 1, strTableName );
        daoUtil.executeQuery( );
        List<String> listColumnsName = new ArrayList<String>( );
        while ( daoUtil.next( ) )
        {
            String strColumnName = daoUtil.getString( 1 );
            listColumnsName.add( strColumnName );
        }
        daoUtil.free( );
        return listColumnsName;
    }

    /**
     * Get the type of a column from its description
     * @param strColumnType The description of the column type
     * @return The column type, or null if no type was found
     */
    protected static ColumnType getJavaTypeFromSqlString( String strColumnType )
    {
        ColumnType columnType = null;
        if ( strColumnType == null || StringUtils.isBlank( strColumnType ) )
        {
            return null;
        }
        String strColumnTypeSearch = strColumnType.toLowerCase( );
        if ( StringUtils.contains( strColumnTypeSearch, CONSTANT_SQL_LONG ) )
        {
            columnType = ColumnType.TYPE_LONG;
        }
        else if ( StringUtils.contains( strColumnTypeSearch, CONSTANT_SQL_INT ) )
        {
            columnType = ColumnType.TYPE_INT;
        }
        else if ( StringUtils.contains( strColumnTypeSearch, CONSTANT_SQL_VARCHAR )
                || StringUtils.contains( strColumnTypeSearch, CONSTANT_SQL_TEXT )
                || StringUtils.contains( strColumnTypeSearch, CONSTANT_SQL_CHARACTER ) )
        {
            columnType = ColumnType.TYPE_STRING;
        }
        else if ( StringUtils.contains( strColumnTypeSearch, CONSTANT_SQL_TIMESTAMP ) )
        {
            columnType = ColumnType.TYPE_TIMESTAMP;
        }
        else if ( StringUtils.contains( strColumnTypeSearch, CONSTANT_SQL_DATE ) )
        {
            columnType = ColumnType.TYPE_DATE;
        }
        else if ( StringUtils.contains( strColumnTypeSearch, CONSTANT_SQL_DOUBLE )
                || StringUtils.contains( strColumnTypeSearch, CONSTANT_SQL_REAL ) )
        {
            columnType = ColumnType.TYPE_DOUBLE;
        }
        else if ( StringUtils.contains( strColumnTypeSearch, CONSTANT_SQL_BYTE )
                || StringUtils.contains( strColumnTypeSearch, CONSTANT_SQL_BLOB ) )
        {
            columnType = ColumnType.TYPE_BYTE;
        }

        if ( columnType != null )
        {
            return columnType;
        }

        return null;
    }
}
