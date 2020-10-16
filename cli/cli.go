package cli

import "flag"

func ProcessCmdOptions() string {
	var activeProfile = flag.String(
		"profile",
		"default",
		"specify active profile, e.g. --profile test",
	)

	flag.Parse()

	return *activeProfile
}
