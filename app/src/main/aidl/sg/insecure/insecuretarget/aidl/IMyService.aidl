// IMyService.aidl
package sg.insecure.insecuretarget.aidl;

// Declare any non-default types here with import statements

interface IMyService {
    /** Request the process ID of this service. */
    int getPid();
    String getData();
    void setData(String data);
    String getPackageName();
}