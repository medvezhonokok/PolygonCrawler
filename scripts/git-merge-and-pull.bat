@echo off

cd /d %1

call git checkout master
call git pull

