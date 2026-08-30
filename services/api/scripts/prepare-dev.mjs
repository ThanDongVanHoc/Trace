import { copyFileSync, existsSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { spawnSync } from 'node:child_process';

const apiDirectory = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const repositoryDirectory = resolve(apiDirectory, '..', '..');
const environmentFile = resolve(repositoryDirectory, '.env');
const environmentExample = resolve(repositoryDirectory, '.env.example');
const composeFile = resolve(repositoryDirectory, 'compose.yaml');

if (!existsSync(environmentFile)) {
  copyFileSync(environmentExample, environmentFile);
  process.stdout.write(
    'Created .env from .env.example for local development.\n',
  );
} else {
  const existingEnvironment = readFileSync(environmentFile, 'utf8');
  const oldDefaultDatabaseUrl =
    'DATABASE_URL=postgresql://trace:trace_dev_password@localhost:5432/trace';
  if (existingEnvironment.includes(oldDefaultDatabaseUrl)) {
    writeFileSync(
      environmentFile,
      existingEnvironment.replace(
        oldDefaultDatabaseUrl,
        'DATABASE_URL=postgresql://trace:trace_dev_password@localhost:55432/trace',
      ),
      'utf8',
    );
    process.stdout.write(
      'Moved the TRACE development database from port 5432 to 55432.\n',
    );
  }
}

if (process.env.TRACE_SKIP_DEV_DB === '1') {
  process.stdout.write(
    'TRACE_SKIP_DEV_DB=1: using the externally managed database.\n',
  );
  process.exit(0);
}

const result = spawnSync(
  'docker',
  ['compose', '-f', composeFile, 'up', '-d', '--wait', 'postgres'],
  {
    cwd: repositoryDirectory,
    encoding: 'utf8',
    stdio: 'inherit',
    windowsHide: true,
  },
);

if (result.error) {
  process.stderr.write(
    `Could not start PostgreSQL. Make sure Docker Desktop is running. ${result.error.message}\n`,
  );
  process.exit(1);
}

if (result.status !== 0) {
  process.stderr.write(
    'Could not start PostgreSQL. Make sure Docker Desktop is running.\n',
  );
  process.exit(result.status ?? 1);
}
