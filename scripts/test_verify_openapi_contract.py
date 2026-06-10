import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT_PATH = Path(__file__).with_name("verify_openapi_contract.py")


class VerifyOpenApiContractTest(unittest.TestCase):

    def test_accepts_identical_openapi_contracts(self) -> None:
        result = self.run_verifier({"openapi": "3.1.0"}, {"openapi": "3.1.0"})

        self.assertEqual(0, result.returncode, result.stderr)

    def test_rejects_contract_drift(self) -> None:
        result = self.run_verifier(
            {"openapi": "3.1.0", "paths": {}},
            {"openapi": "3.1.0", "paths": {"/api/users/me": {}}},
        )

        self.assertNotEqual(0, result.returncode)
        self.assertIn("OpenAPI contract drift detected", result.stderr)

    def test_rejects_invalid_openapi_document(self) -> None:
        result = self.run_verifier({"name": "not-openapi"}, {"name": "not-openapi"})

        self.assertNotEqual(0, result.returncode)
        self.assertIn("Expected an OpenAPI 3.x document", result.stderr)

    def run_verifier(self, committed: dict, generated: dict) -> subprocess.CompletedProcess[str]:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            committed_path = root / "committed.json"
            generated_path = root / "generated.json"
            committed_path.write_text(json.dumps(committed), encoding="utf-8")
            generated_path.write_text(json.dumps(generated), encoding="utf-8")

            return subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT_PATH),
                    str(committed_path),
                    str(generated_path),
                ],
                capture_output=True,
                text=True,
                check=False,
            )


if __name__ == "__main__":
    unittest.main()
