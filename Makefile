default: up

up: dockerComposeBuild runDockerCompose readLogs

dockerComposeBuild:
	docker-compose build

runDockerCompose:
	docker-compose up -d ; docker-compose ps ; echo "use 'docker-compose logs -f <container> to tail container logs'"

down:
	docker-compose down

readLogs:
	docker-compose logs -f console-downstream-consumer