package com.claudecodemobile.setup

enum class SetupStep(val title: String, val detail: String) {
    CheckStorage("Check storage", "Needs about 1.2 GB of free space"),
    DownloadUbuntu("Download Ubuntu", "Ubuntu 24.04 base system, about 30 MB"),
    ExtractUbuntu("Unpack Ubuntu", "About 90 MB on disk"),
    ConfigureLinux("Configure Linux", "Networking and the package manager"),
    InstallTools("Install tools", "git, ripgrep, curl, and certificates"),
    InstallNode("Install Node.js", "Node.js 24 LTS from nodejs.org"),
    InstallClaudeCode("Install Claude Code", "Latest version from npm"),
    Verify("Check Claude Code", "Makes sure claude starts"),
}
