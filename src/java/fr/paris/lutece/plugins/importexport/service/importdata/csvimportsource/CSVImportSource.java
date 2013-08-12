package fr.paris.lutece.plugins.importexport.service.importdata.csvimportsource;

import fr.paris.lutece.plugins.importexport.business.ImportExportElement;
import fr.paris.lutece.plugins.importexport.service.importdata.IImportSource;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.string.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;


/**
 * Source to import data from CSV files.
 */
public class CSVImportSource implements IImportSource
{
    private static final char CONSTANT_BOM_UTF8 = 65279;

    private CSVReader _csvReader;
    private Reader _reader;
    private List<String> _listColumnsName;

    /**
     * Creates a new CSVImportSource from a file uploaded by the user
     * @param fileItem The CSV file to read data from. The first line of the CSV
     *            file must contain the names of columns of the database
     * @param strCSVSeparator The CSV separator to use. If it is null or empty,
     *            then the default CSV separator is used (which is ",")
     * @param strCSVQuoteChar The CSV quote character to use. If it is null or
     *            empty, then the default CSV quote character is used (which is
     *            "\"")
     */
    public CSVImportSource( FileItem fileItem, String strCSVSeparator, String strCSVQuoteChar )
    {
        if ( fileItem != null )
        {

            try
            {
                _reader = new InputStreamReader( fileItem.getInputStream( ) );
            }
            catch ( IOException e )
            {
                AppLogService.error( e.getMessage( ), e );
            }
            if ( _reader != null )
            {
                Character cSeparator = StringUtils.isNotEmpty( strCSVSeparator ) ? strCSVSeparator.charAt( 0 )
                        : CSVReader.DEFAULT_SEPARATOR;
                Character cQuoteChar = StringUtils.isNotEmpty( strCSVQuoteChar ) ? strCSVQuoteChar.charAt( 0 )
                        : CSVReader.DEFAULT_QUOTE_CHARACTER;
                _csvReader = new CSVReader( _reader, cSeparator, cQuoteChar );
            }
        }
    }

    /**
     * Creates a new CSVImportSource from a file of the server
     * @param file The CSV file to read data from. The first line of the CSV
     *            file must contain the names of columns of the database
     * @param strCSVSeparator The CSV separator to use. If it is null or empty,
     *            then the default CSV separator is used (which is ",")
     * @param strCSVQuoteChar The CSV quote character to use. If it is null or
     *            empty, then the default CSV quote character is used (which is
     *            "\"")
     */
    public CSVImportSource( File file, String strCSVSeparator, String strCSVQuoteChar )
    {
        if ( file != null )
        {
            try
            {
                _reader = new FileReader( file );
                Character cSeparator = StringUtils.isNotEmpty( strCSVSeparator ) ? strCSVSeparator.charAt( 0 )
                        : CSVReader.DEFAULT_SEPARATOR;
                Character cQuoteChar = StringUtils.isNotEmpty( strCSVQuoteChar ) ? strCSVQuoteChar.charAt( 0 )
                        : CSVReader.DEFAULT_QUOTE_CHARACTER;
                _csvReader = new CSVReader( _reader, cSeparator, cQuoteChar );
            }
            catch ( FileNotFoundException e )
            {
                AppLogService.error( e.getMessage( ), e );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ImportExportElement> getNextValues( )
    {
        // We check that the reader has been initialized
        if ( _csvReader == null )
        {
            return null;
        }
        List<String> listColumnsName = getColumnsName( );
        if ( listColumnsName == null )
        {
            return null;
        }
        String[] strLine;
        try
        {
            strLine = _csvReader.readNext( );
        }
        catch ( IOException e )
        {
            AppLogService.error( e.getMessage( ), e );
            return null;
        }
        if ( strLine != null )
        {
            List<ImportExportElement> listElements = new ArrayList<ImportExportElement>( strLine.length );
            if ( strLine.length != listColumnsName.size( ) )
            {
                // If the number of elements is not correct, then we return an empty list.
                return listElements;
            }
            int i = 0;
            for ( String strColumnTitle : listColumnsName )
            {
                ImportExportElement element = new ImportExportElement( );
                element.setColumnName( strColumnTitle );
                element.setValue( strLine[i] );
                listElements.add( element );
                i++;
            }
            return listElements;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getColumnsName( )
    {
        if ( _listColumnsName != null )
        {
            return _listColumnsName;
        }

        if ( _csvReader == null )
        {
            return null;
        }

        String[] strFirstLine = null;
        try
        {
            strFirstLine = _csvReader.readNext( );
        }
        catch ( IOException e )
        {
            AppLogService.error( e.getMessage( ), e );
            return null;
        }

        _listColumnsName = new ArrayList<String>( strFirstLine.length );
        if ( strFirstLine.length > 0 )
        {
            boolean bIsFirst = true;
            for ( String strColumnTitle : strFirstLine )
            {
                if ( strColumnTitle != null )
                {
                    String strTitle = strColumnTitle.toLowerCase( ).trim( );
                    strTitle = StringUtil.replaceAccent( strTitle );
                    if ( bIsFirst )
                    {
                        // We check that the BOM character has not been read
                        if ( strTitle.startsWith( String.valueOf( CONSTANT_BOM_UTF8 ) ) )
                        {
                            strTitle = strTitle.substring( 1 );
                        }
                        // We eventually remove any first character that is not a latin character
                        else if ( strTitle.charAt( 0 ) > 255 )
                        {
                            strTitle = strTitle.substring( 1 );
                        }

                        // The BOM character can only be the first character of the file
                        bIsFirst = false;
                    }
                    _listColumnsName.add( strTitle );
                }
            }
        }

        return _listColumnsName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close( )
    {
        if ( _csvReader != null )
        {
            try
            {
                _csvReader.close( );
                _csvReader = null;
                _reader.close( );
            }
            catch ( IOException e )
            {
                AppLogService.error( e.getMessage( ), e );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize( ) throws Throwable
    {
        if ( _csvReader != null )
        {
            close( );
        }
        super.finalize( );
    }
}
