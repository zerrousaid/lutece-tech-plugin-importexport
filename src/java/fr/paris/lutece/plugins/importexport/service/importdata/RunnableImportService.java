package fr.paris.lutece.plugins.importexport.service.importdata;

import fr.paris.lutece.plugins.importexport.business.importdata.ImportResult;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppLogService;


/**
 * Service that allow to import data. This service implements the Runnable
 * interface.
 */
public class RunnableImportService implements Runnable
{
    public static final int STATUS_QUEUED = 0;
    public static final int STATUS_WORKING = 0;
    public static final int STATUS_FINISHED = 0;

    private IImportSource _importSource;
    private String _strTableName;
    private Plugin _plugin;
    private boolean _bUpdateExistingRows;
    private int _nStatus = STATUS_QUEUED;
    private volatile ImportResult _importResult;

    /**
     * Service to asynchronously import data
     * @param importSource The data source to get data from
     * @param strTableName The name of the table
     * @param plugin The plugin associated with the pool the table of the
     *            database is in.
     * @param bUpdateExistingRows Indicates whether existing rows should be
     *            updated (true) or ignored (false)
     */
    public RunnableImportService( IImportSource importSource, String strTableName, Plugin plugin,
            boolean bUpdateExistingRows )
    {
        this._importSource = importSource;
        this._strTableName = strTableName;
        this._plugin = plugin;
        this._bUpdateExistingRows = bUpdateExistingRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run( )
    {
        try
        {
            _nStatus = STATUS_WORKING;
            _importResult = ImportManager.doProcessImport( _importSource, _strTableName, _bUpdateExistingRows, _plugin );
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage( ), e );
        }
        finally
        {
            _nStatus = STATUS_FINISHED;
        }
    }

    /**
     * Get the status of the service.
     * @return {@link #STATUS_QUEUED} if the serviced has not been started,
     *         {@link #STATUS_WORKING} if it is executing, or
     *         {@link #STATUS_FINISHED} if its execution has ended.
     */
    public int getServiceStatus( )
    {
        return _nStatus;
    }

    /**
     * Get the result of the importation of this service
     * @return The result of the importation of this service, or null if the
     *         import is not complete or if an error occurred
     */
    public ImportResult getImportResult( )
    {
        return _importResult;
    }
}
