#!/bin/bash
# Script to push to GitHub
# Run this script and enter your GitHub credentials when prompted

echo "Pushing to GitHub repository..."
echo "Repository: https://github.com/bluehawana/weatheranywhere-springboot-mysql-vps-racknerd.git"
echo ""
echo "You will need to provide:"
echo "  Username: your GitHub username"
echo "  Password: your GitHub Personal Access Token (not your password!)"
echo ""
echo "Don't have a token? Create one at: https://github.com/settings/tokens"
echo ""

git push -u origin main

echo ""
echo "Done! Check your repository at:"
echo "https://github.com/bluehawana/weatheranywhere-springboot-mysql-vps-racknerd"
