package dev.lemonclient.utils.others;
/*
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;

public interface Kernel32DLL extends Library {
    Kernel32DLL INSTANCE = Native.loadLibrary("kernel32", Kernel32DLL.class);

    boolean SetConsoleTitleA(String title);

    boolean Beep(int frequency, int duration);

    void CopyMemory(byte[] destination, byte[] source, long length);

    void GetSystemTime(WinBase.SYSTEMTIME result);

    int GetCurrentThreadId();

    int GetCurrentProcessId();

    int OpenProcess(int dwDesiredAccess, boolean bInheritint, int dwProcessId);

    boolean Closeint(int hObject);

    boolean ReadProcessMemory(int hProcess, long lpBaseAddress, byte[] lpBuffer, int nSize, int[] lpNumberOfBytesRead);

    boolean WriteProcessMemory(int hProcess, long lpBaseAddress, byte[] lpBuffer, int nSize, int[] lpNumberOfBytesWritten);

    long VirtualAllocEx(int hProcess, long lpAddress, int dwSize, int flAllocationType, int flProtect);

    boolean VirtualFreeEx(int hProcess, long lpAddress, int dwSize, int dwFreeType);

    boolean VirtualProtectEx(int hProcess, long lpAddress, int dwSize, int flNewProtect, int[] lpflOldProtect);

    boolean VirtualQueryEx(int hProcess, long lpAddress, WinNT.MEMORY_BASIC_INFORMATION lpBuffer, int dwLength);

    int CreateRemoteThread(int hProcess, long lpThreadAttributes, int dwStackSize, long lpStartAddress, long lpParameter, int dwCreationFlags, int[] lpThreadId);

    int CreateRemoteThread(int hProcess, long lpThreadAttributes, int dwStackSize, int lpStartAddress, long lpParameter, int dwCreationFlags, int[] lpThreadId);

    int OpenThread(int dwDesiredAccess, boolean bInheritint, int dwThreadId);

    int SuspendThread(int hThread);

    void CloseHandle(int hProcess);

    int GetModuleHandle(String lpName);

    int ResumeThread(int hThread);

    boolean TerminateThread(int hThread, int dwExitCode);

    boolean FlushInstructionCache(int hProcess, long lpBaseAddress, int dwSize);

    boolean GetExitCodeThread(int hThread, int[] lpExitCode);

    int CreateToolhelp32Snapshot(int dwFlags, int th32ProcessID);

    boolean Process32First(int hSnapshot, Tlhelp32.PROCESSENTRY32 lppe);

    boolean Process32Next(int hSnapshot, Tlhelp32.PROCESSENTRY32 lppe);

    boolean Thread32Next(int hSnapshot, Tlhelp32.THREADENTRY32 lppe);

    boolean ContinueThread(int hThread);

    int WaitForSingleObject(int hint, int dwMilliseconds);

    int WaitForMultipleObjects(int nCount, int[] lpints, boolean bWaitAll, int dwMilliseconds);

    void Sleep(int dwMilliseconds);

    int LoadLibraryEx(String lpFileName);

    long GetProcAddress(long hModule, String lpProcName);

    void DisableThreadLibraryCalls(long hModule);
}
*/
