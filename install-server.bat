git clone https://github.com/nats-io/nats-server.git
cd nats-server
if "%1" == "latest" goto aftertag
git checkout tags/%1 -b nstagged
:aftertag
go get
go build main.go
cd ..
copy /Y nats-server\main.exe nats-server.exe
rd /S /Q nats-server
nats-server -v
