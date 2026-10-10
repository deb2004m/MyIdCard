$ErrorActionPreference = "Stop"

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "Maven was not found on PATH. Install Maven or add its bin directory to PATH."
}

$envFile = Join-Path $PSScriptRoot ".env"
if (Test-Path $envFile) {
    $identity = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
    $acl = Get-Acl $envFile
    $acl.SetAccessRuleProtection($true, $false)
    $acl.Access | ForEach-Object { [void]$acl.RemoveAccessRule($_) }
    $userRule = [System.Security.AccessControl.FileSystemAccessRule]::new(
        $identity,
        [System.Security.AccessControl.FileSystemRights]::FullControl,
        [System.Security.AccessControl.AccessControlType]::Allow
    )
    $systemRule = [System.Security.AccessControl.FileSystemAccessRule]::new(
        "SYSTEM",
        [System.Security.AccessControl.FileSystemRights]::FullControl,
        [System.Security.AccessControl.AccessControlType]::Allow
    )
    $acl.AddAccessRule($userRule)
    $acl.AddAccessRule($systemRule)
    Set-Acl -Path $envFile -AclObject $acl

    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $name, $value = $line.Split("=", 2)
            $name = $name.Trim()
            $value = $value.Trim()
            if (($value.StartsWith('"') -and $value.EndsWith('"')) -or
                ($value.StartsWith("'") -and $value.EndsWith("'"))) {
                $value = $value.Substring(1, $value.Length - 2)
            }
            if ($name -match '^(DB_URL|DB_USERNAME|DB_PASSWORD|UPLOAD_DIRECTORY)$') {
                [Environment]::SetEnvironmentVariable($name, $value, "Process")
            }
        }
    }
}

$databaseHost = "localhost"
$databasePort = 3306
if (-not (Test-NetConnection $databaseHost -Port $databasePort -WarningAction SilentlyContinue).TcpTestSucceeded) {
    throw "MySQL is not reachable at ${databaseHost}:$databasePort. Start the MySQL service and try again."
}

if (-not $env:DB_USERNAME) {
    $env:DB_USERNAME = "root"
}

if (-not $env:DB_PASSWORD) {
    $securePassword = Read-Host "MySQL password for $($env:DB_USERNAME) (input is hidden)" -AsSecureString
    $env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
}

try {
    mvn spring-boot:run
    exit $LASTEXITCODE
}
finally {
    Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
    Remove-Variable securePassword -ErrorAction SilentlyContinue
}
