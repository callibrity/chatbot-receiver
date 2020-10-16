if [ -d "./generated" ]
then
	rm -rf ./generated/*
else
	mkdir -p ./generated
fi

protoc -I resources/protos/ resources/protos/*.proto --go_out=plugins=grpc:./
