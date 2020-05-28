package backup;

import java.beans.PropertyVetoException;

public interface BackupObj {
    public String toBackupString();

    public void fromBackupString(String backup);
}
